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
        for _ in range(1, 11):
            msg=input("Enter your message to be sent: ")
            self.socket.send_string(msg)
            message=self.socket.recv_multipart()
            print(f"Received reply {message} to {msg}")


if __name__=='__main__':
    url="127.0.0.1"
    port=8080
    client=Client(url=url, port=port)
    client.receive()