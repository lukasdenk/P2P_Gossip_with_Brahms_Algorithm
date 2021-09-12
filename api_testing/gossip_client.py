#!/usr/bin/python3

import argparse
import hexdump
import socket
import struct
import threading
import time
from gossip_testing.util import connect_socket, sync_read_message
from typing import List

GOSSIP_ANNOUNCE = 500
GOSSIP_NOTIFY = 501
GOSSIP_NOTIFICATION = 502
GOSSIP_VALIDATION = 503


class GossipClient:
    def __init__(self, id):
        self.gossip_addr = "127.0.0.1"
        self.port = 7050 + id
        self.rec = {}
        self.s: socket.socket = connect_socket(self.gossip_addr, self.port)
        self.id = id
        print(f"init id {id}")

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
        try:
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
                self.rec[dtype] = self.rec.get(dtype, 0) + 1

                print(f"[{self.port}] Send VALIDATION for {mid}")
                self.send_gossip_validation(mid, validation)

                hexdump.hexdump(buf)
        except (ConnectionAbortedError, UnboundLocalError):
            pass

    def wait_notification(self, validation=True):
        """
        Wait for a subsequent GOSSIP_NOTIFICATION message

            s:          connected socket to listen on
            returns:    message ID of notification message
        """
        p = threading.Thread(target=self.threaded_wait_notification, args=(validation,))
        p.start()

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

    def __repr__(self):
        return str(self)

    def __eq__(self, o: object) -> bool:
        return o is not None and isinstance(o, GossipClient) and o.id == self.id

    def __hash__(self) -> int:
        return hash(self.id)

    def client_close(self):
        print(f'closed {self.port}')
        self.s.close()


def send_and_wait(instances: List[GossipClient], dtype, validation=True):
    print(f"Instances {instances[1:]} will send 'GOSSIP NOTIFY' for data type {dtype}. "
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
    time.sleep(8)
    failed = False
    for instance in instances[1:]:
        count = instance.rec.get(dtype, 0)
        if dtype not in instance.rec:
            print(f'[NOTIFICATIONS] {instance} did not receive {dtype}.')
            failed = True
        else:
            print(f'[NOTIFICATIONS] {instance} received {count} msgs of type {dtype}.')
    if failed:
        print('WARNING: [NOTIFICATIONS] Test complete. Some instances did not receive all messages. This could be due '
              'to a bug. However, it could also be due to GOSSIP\'s best effort strategy to send messages.')
    else:
        print('[NOTIFICATIONS] Test complete. No errors could be detected.')


def check_validation(instances: List[GossipClient]):
    print("Testing if GOSSIP spreads invalid gossip messages...")
    dtype = 2
    send_and_wait(instances, dtype, False)
    instances[0].send_gossip_announce(b"invalid", 8, dtype, 0)
    time.sleep(8)
    received = instances[-1].rec.get(dtype, 0)
    if received != 1:
        print(f'ERROR: Test FAILED. {instances[-1]} received the wrong number of data type {dtype} msgs: \n'
              f'\tReceived: {received}\n'
              f'\tExpected: 1 (the message from the instance which has sent the "GOSSIP_ANNOUNCE" to spread the message)')
    else:
        print('Test complete. No errors could be detected.')




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
        b'Inval', 8,
        dtype_not_for_last_instance, 0)
    instances[0].send_gossip_announce(b'Val',
                                      8, dtype_for_all_instances, 0)

    time.sleep(8)

    print('Test complete. Results:')
    if dtype_not_for_last_instance in instances[-1].rec:
        print(f'ERROR: Instance {last_instance} received {dtype_not_for_last_instance} without subscribing for it.')
    if dtype_for_all_instances in instances[-1].rec:
        print(f'Instance {last_instance} successfully received {dtype_for_all_instances}.')
    else:
        print(f'WARNING: {last_instance} did not receive {dtype_for_all_instances} even though subscribing for it. '
              f'Probably an error but could also be to GOSSIP\'s best effort strategy to spread messages.')




def main():
    # parse command line arguments
    usage_string = "Test the Gossip Module"
    cmd = argparse.ArgumentParser(description=usage_string)

    test_modes = ["notifications", "validations", "subscriptions"]
    cmd.add_argument("-t", "--test_mode", help="Test mode. Possibilities:\n"
                                               "***(default) notifications: Tests if message spreading and 'GOSSIP NOTIFY' works.***\n"
                                               "validations: Tests if GOSSIP spreads invalid gossip messages.***\n"
                                               "subscriptions: Tests if GOSSIP only sends 'GOSSIP NOTIFICATION's for subscribed topics.***",
                     choices=test_modes, default="notifications")
    args = cmd.parse_args()

    number_instances = 3

    print(
        f"INFO: Testing only works if ports 7000-{6999 + number_instances} and 7050-{7049 + number_instances} are free.")

    instances = [GossipClient(id) for id in range(number_instances)]

    if args.test_mode == "notifications":
        check_notifications(instances)
    elif args.test_mode == "validations":
        check_validation(instances)
    elif args.test_mode == "subscriptions":
        check_subscriptions(instances)

    for instance in instances:
        instance.client_close()
    print("Terminated all clients.")


if __name__ == "__main__":
    main()
