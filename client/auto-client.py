import zmq
from random import choice, randint
from time import sleep

class AutoClient:
    __slots__=['context', 'socket']
    def __init__(self, url: str, port:int):
        #  Prepare our context and sockets
        self.context:zmq.context = zmq.Context()
        self.socket:zmq.Socket = self.context.socket(zmq.REQ)
        self.socket.connect("tcp://{url}:{port}".format(**locals()))

    def receive(self):
        #  Do 10 requests, waiting each time for a response
        while (True):
            sleep(randint(0, 10))
            # Automatic message
            msg=choice(["HolaMundo", "help", "consult", "NotConsult", "HelloWorld", "user1,NotThePassword,1"])
            self.socket.send_string(msg)
            message=self.socket.recv()
            print(f"Received reply \n\n{message.decode()}\n\nto {msg}")


if __name__=='__main__':
    url="load-balancer"
    port=8080
    client=AutoClient(url=url, port=port)
    client.receive()
