import zmq

url="localhost"
port=8080
#  Prepare our context and sockets
context = zmq.Context()
socket:zmq.Socket = context.socket(zmq.REQ)
socket.connect("tcp://{url}:{port}".format(**locals()))

#  Do 10 requests, waiting each time for a response
for request in range(1, 11):
    socket.send_string("Hello from client")
    print ("Waiting here")
    print ("Waiting here - 2")
    message=socket.recv_multipart()
    print(f"Received reply {request} [{message}]")