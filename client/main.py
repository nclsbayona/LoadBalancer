import zmq

#  Prepare our context and sockets
context = zmq.Context()
socket:zmq.Socket = context.socket(zmq.REQ)
socket.connect("tcp://localhost:8080")

#  Do 10 requests, waiting each time for a response
for request in range(1, 11):
    socket.send_string("Hello from client")
    message = socket.recv()
    print(f"Received reply {request} [{message}]")