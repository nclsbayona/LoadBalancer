import zmq

url="localhost"
port=30216
#  Prepare our context and sockets
context = zmq.Context()
socket:zmq.Socket = context.socket(zmq.REP)
socket.connect("tcp://{url}:{port}".format(**locals()))

while True:
    message = socket.recv()
    print(f"Received request: {message}")
    socket.send_string("World")