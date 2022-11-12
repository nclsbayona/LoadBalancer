import zmq
from random import choice
from time import time

class AutoClient:
    __slots__=['context', 'socket']
    def __init__(self, url: str, port:int):
        #  Prepare our context and sockets
        self.context:zmq.context = zmq.Context()
        self.socket:zmq.Socket = self.context.socket(zmq.REQ)
        self.socket.connect("tcp://{url}:{port}".format(**locals()))

    def receive(self):
        amount=5
        start_time=time()
        for _ in range(amount):
            # Automatic message
            msg=choice(["HolaMundo", "help", "consult", "user1,NotThePassword,1"])
            self.socket.send_string(msg)
            message=self.socket.recv()
            print(f"Received reply \n\n{message.decode()}\n\nto {msg}")
        elapsed_time=time()-start_time
        print ("Elapsed time: %.10f seconds." % elapsed_time)
        
    def __del__(self):
        self.socket.close()


if __name__=='__main__':
    url="192.168.10.29"#"load-balancer"
    port=8080
    client=AutoClient(url=url, port=port)
    try:
        client.receive()
    except:
        del client
