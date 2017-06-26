import numpy as np
import sys

from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler

syn0 = 0
syn1 = 0

def get_number(output_array):
    global max_index

    max_index = 0
    for i in range(0, len(output_array)):
        if (output_array[i] > output_array[max_index]):
            max_index = i

    return [max_index, output_array[max_index]]

def nonlin(x,deriv=False):
    if(deriv==True):
        return x * (1-x)
    return 1/(1+np.exp(-x))

def load_weights():
    global syn0
    global syn1

    syn0 = np.loadtxt(open("mnist_syn0.txt", "rb"), delimiter=",", skiprows=0)
    syn1 = np.loadtxt(open("mnist_syn1.txt", "rb"), delimiter=",", skiprows=0)

def resolve_image(pixels):
    max_len = 28 * 28
    local_x = np.empty(max_len + 1)

    for i in range(0, len(pixels)):
        local_x[i] = pixels[i]

    for i in range(0, 28):
      linestr = " "
      for j in range(0, 28):
        linestr += str(int(local_x[i * 28 + j])) + " "
      print(linestr)

    local_x[max_len] = -1

    l0 = local_x
    l1 = nonlin(np.dot(l0, syn0))

    l1 = np.concatenate(([l1, [-1]]))

    l2 = nonlin(np.dot(l1, syn1))

    print("l2: " + str(l2))

    result = get_number(l2)

    return result


class RequestHandler(BaseHTTPRequestHandler):
    
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write("NN handwritten recognition server runnin")
        self.wfile.close()
        
    def do_POST(self):
        
        content_length = self.headers.getheaders('content-length')
        length = int(content_length[0]) if content_length else 0
        
        data = self.rfile.read(length);

        image_array = []

        for i in range(0, len(data)):
          if (data[i] == "0"):
            image_array.append(0)
          elif (data[i] == "1"):
            image_array.append(1)

        result = resolve_image(image_array)

        print("Result: " + str(result))

        self.send_response(200)
        self.end_headers()
        self.wfile.write(str(result[0]) + "," + str(int(result[1] * 100)))
        self.wfile.close()
    
        
def main():
    if (len(sys.argv) > 1):
        port = int(sys.argv[1])
    else:
        port = 8080

    print('Listening on localhost: '  + str(port))

    server = HTTPServer(('', port), RequestHandler)
    server.serve_forever()

        
if __name__ == "__main__":
    load_weights()
    main()