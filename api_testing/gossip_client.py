#!/usr/bin/python3

import argparse
import hexdump
import socket
import struct
import threading
import time
from typing import List

import util
from util import connect_socket, sync_read_message

GOSSIP_ANNOUNCE = 500
GOSSIP_NOTIFY = 501
GOSSIP_NOTIFICATION = 502
GOSSIP_VALIDATION = 503


class GossipClient:
    def __init__(self, id):
        self.gossip_addr = "127.0.0.1"
        self.port = 7050 + id
        self.received = {}
        self.s: socket.socket = connect_socket(self.gossip_addr, self.port)
        self.id = id

    def send_gossip_announce(self, data_cont, data_ttl, data_type, instance):
        """
        Send a GOSSIP_ANNOUNCE message to socket s

            s:          connected socket to send message on
        """

        # prepare packet payload
        bsize = 4 + 4 + len(data_cont)
        buf = struct.pack(">HHBBH", bsize, GOSSIP_ANNOUNCE, data_ttl, 0, data_type)
        buf += data_cont

        hexdump.hexdump(buf)

        # Send payload

        print(f"Instance {instance} sends 'GOSSIP ANNOUNCE' for data type {data_type}.")
        self.s.send(buf)

    def send_gossip_notify(self, data_type):
        """
        Send a GOSSIP_NOTIFY message to socket s
            s:          connected socket to send message on
        """
        # prepare packet payload
        bsize = 4 + 4
        buf = struct.pack(">HHHH", bsize, GOSSIP_NOTIFY, 0, data_type)

        # Send payload
        self.s.send(buf)
        print(f"[{self.port}] [+] Sent GOSSIP_NOTIFY for type {data_type}).")

    def threaded_wait_notification(self, validation):
        while True:
            # Try to read notification for our registered DATA_TYPE
            buf = sync_read_message(self.s)
            msize, mtype, mid, dtype = struct.unpack(">HHHH", buf[:8])
            mdata = buf[8:]

            if mtype != GOSSIP_NOTIFICATION:
                reason = f"[{self.port}] Wrong packet type: {mtype}"
                # print(reason)
                # sync_bad_packet(buf, self.s, reason)
                continue

            print(f"[{self.port}] [+] Got GOSSIP_NOTIFICATION: mID = {mid}, type = {dtype}, "
                  + f"data = {mdata}")
            self.received[dtype] = self.received.get(dtype, 0) + 1

            print(f"[{self.port}] Send VALIDATION for {mid}")
            self.send_gossip_validation(mid, validation)

            hexdump.hexdump(buf)

    def wait_notification(self, validation=True):
        """
        Wait for a subsequent GOSSIP_NOTIFICATION message

            s:          connected socket to listen on
            returns:    message ID of notification message
        """
        t = threading.Thread(target=self.threaded_wait_notification, args=(validation,))
        t.start()

    def send_gossip_validation(self, mid, valid):
        """
        Send a GOSSIP_VALIDATION message to socket s

            s:          connected socket to send message on
            mid:        message ID to validate
            valid:      bool, signifying if message MID was valid
        """
        # prepare packet payload
        bsize = 4 + 4
        buf = struct.pack(">HHHBB", bsize, GOSSIP_VALIDATION, mid, 0, valid)

        # Send payload
        self.s.send(buf)
        validstr = "Valid" if valid else "Invalid"
        print(f"[{self.port}] [+] Sent GOSSIP_VALIDATION: mid = {mid}, {validstr}).")

    def __str__(self) -> str:
        return f'GossipClient(id={self.id})'

    def __eq__(self, o: object) -> bool:
        return o is not None and isinstance(o, GossipClient) and o.id == self.id

    def __hash__(self) -> int:
        return hash(self.id)

    def reset(self):
        self.received = {}
        print(f'closed {self.port}')
        self.s.close()
        self.s = util.connect_socket(self.gossip_addr, self.port)


class GossipPeerTester:
    def __init__(self, id):
        self.id = id
        filepath = f"{id}.ini"
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write("[gossip]\n" +
                    "degree = 30\n" +
                    "cache_size = 50\n" +
                    f"api_address = localhost:{7001 + id * 2}\n" +
                    f"p2p_address = localhost:{7002 + 2 * id}\n" +
                    "bootstrapper = localhost:7000")


def send_and_wait(instances: List[GossipClient], dtype, validation=True):
    print(f"Instances {instances} will send 'GOSSIP NOTIFY' for data type {dtype}. "
          f"They will wait for 'GOSSIP NOTIFICATION' messages of this type. "
          f"They will {'' if validation else 'in'}validate these messages.")
    for instance in instances:
        if instance.id == 0:
            continue
        instance.send_gossip_notify(dtype)
        instance.wait_notification(validation)


def check_notifications(instances: List[GossipClient], ttl=8):
    print("Testing if message spreading and 'GOSSIP NOTIFY' works...")
    dtype = 1
    send_and_wait(instances, dtype)
    instances[0].send_gossip_announce(b"test notifications", 8, 1, 0)
    time.sleep(3)
    failed = False
    for instance in instances[1:]:
        count = instance.received.get(dtype, 0)
        if dtype not in instance.received:
            print(f'[NOTIFICATIONS] {instance} did not receive {dtype}.')
            failed = True
        else:
            print(f'[NOTIFICATIONS] {instance} received {count} msgs of type {dtype}.')
    if failed:
        print('WARNING: [NOTIFICATIONS] Test complete. Some instances did not receive all messages. This could be due '
              'to a bug. However, it could also be due to GOSSIP\'s best effort strategy to send messages.')
    else:
        print('[NOTIFICATIONS] Test complete. No errors could be detected.')


def received(instances: List[GossipClient], expected: List[GossipClient], dtype) -> List[GossipClient]:
    received = []
    for instance in instances:
        if dtype in instance.received:
            received += [instance]
    for instance in instances:
        if instance in received and instance not in expected:
            print(f"{instance} got unexpected ")
    return received


def check_validation(instances: List[GossipClient]):
    print("Testing if GOSSIP spreads invalid gossip messages...")
    dtype = 2
    send_and_wait(instances, dtype, False)
    instances[0].send_gossip_announce(b"invalid", 8, dtype, 0)
    time.sleep(2)
    received = instances[-1].received.get(dtype, 0)
    if received != 1:
        print(f'ERROR: Test FAILED. A GOSSIP instance received the wrong number of {dtype} msgs: \n'
              f'\tReceived: {received}\n'
              f'\tExpected: 1 (the message from the instance which has sent the "GOSSIP_ANNOUNCE" to spread the message)')
    else:
        print('Test complete. No errors could be detected.')


def check_channel_close(instances: List[GossipClient]):
    dtype = 4
    check_notifications(instances)
    instances[0].send_gossip_announce(b"[UNSUB] receive", 8, dtype, 0)
    instances[-1].reset()
    time.sleep(2)
    send_and_wait(instances, dtype, True)
    if dtype in instances[-1].received:
        print(f'[UNSUB] received notification for dtype {dtype} without subscription')
    print('[UNSUB] ++++++++++++')


def check_keep_channel_open(instances: List[GossipClient]):
    for i in range(10):
        check_notifications(instances)
        time.sleep(4)


def check_subscriptions(instances: List[GossipClient]):
    print("Tests if GOSSIP only sends 'GOSSIP NOTIFICATION's for subscribed topics...")
    last_instance = len(instances) - 2
    dtype_not_for_last_instance = 4
    dtype_for_all_instances = 5

    print(f"All but instance {last_instance} will subscribe for data type {dtype_not_for_last_instance}.\n"
          f"All instances will subscribe for data type {dtype_for_all_instances}.")

    send_and_wait(instances[:-1], dtype_not_for_last_instance)
    send_and_wait(instances, dtype_for_all_instances)
    instances[0].send_gossip_announce(
        b'Instance "' + last_instance.to_bytes() + b'" did not subscribe for this datatype.', 8,
        dtype_not_for_last_instance, 0)
    instances[0].send_gossip_announce(b'Instance "' + last_instance.to_bytes() + b'" did subscribe for this datatype.',
                                      8, dtype_for_all_instances, 0)

    time.sleep(3)

    print('Test complete. Results:')
    if dtype_not_for_last_instance in instances[-1].received:
        print(f'ERROR: Instance {last_instance} received {dtype_not_for_last_instance} without subscribing for it.')
    if dtype_for_all_instances in instances[-1].received:
        print(f'Instance {last_instance} successfully received {dtype_for_all_instances}.')
    else:
        print(f'WARNING: {last_instance} did not received {dtype_for_all_instances} even though subscribing for it. '
              f'Probably an error but could also be to GOSSIP\'s best effort strategy to spread messages.')


def main():
    # parse command line arguments
    usage_string = "Test the Gossip Module"
    cmd = argparse.ArgumentParser(description=usage_string)
    cmd.add_argument("-i", "--instances", type=int, default=3)

    test_modes = ["notifications", "validations", "subscriptions"]
    cmd.add_argument("-t", "--test_mode", help="Test mode.Possibilities:\n"
                                               "(default) notifications: Tests if message spreading and 'GOSSIP NOTIFY' works.\n"
                                               "validations: Tests if GOSSIP spreads invalid gossip messages.\n"
                                               "subscriptions: Tests if GOSSIP only sends 'GOSSIP NOTIFICATION's for subscribed topics.",
                     choices=test_modes, default="notifications")
    args = cmd.parse_args()

    # TODO: ini for testing
    # ports depending on ini

    number_instances = args.instances

    instances = [GossipClient(id) for id in range(number_instances)]

    if args.test_mode == "notifications":
        check_notifications(instances)
    elif args.test_mode == "validations":
        check_validation(instances)
    elif args.test_mode == "subscriptions":
        check_subscriptions(instances)


if __name__ == "__main__":
    main()
