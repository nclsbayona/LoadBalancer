import zmq

class Client:
    __slots__=['context', 'socket']
    def __init__(self, url: str, port:int):
        #  Prepare our context and sockets
        self.context:zmq.context = zmq.Context()
        self.socket:zmq.Socket = self.context.socket(zmq.REQ)
        self.socket.connect("tcp://{url}:{port}".format(**locals()))

    def receive(self):
        #  Do 10 requests, waiting each time for a response
        while (True):
            msg=input("Enter your message to be sent: ")
            self.socket.send_string(msg)
            print (f"Just send {msg}")
            message=self.socket.recv()
            print(f"Received reply \n\n{message.decode()}\n\nto {msg}")

    def __del__(self):
        self.socket.close()

if __name__=='__main__':
    url="192.168.10.29"
    port=8080
    client=Client(url=url, port=port)
    try:
        client.receive()
    except:
        del client
