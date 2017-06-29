import numpy as np
from mnistparser import read_data

n = 0.001 # 0.0005
syn0 = 0 # Input layer weights
syn1 = 0 # Hidden layer weights
hd = 128 # Hidden neurons
epochs = 1200 # Max epochs to iterate over each image
DATA_SIZE = 60000 # Data size to recover from NIST databse, 60000 = all images
MNIST_PATH = 'python-mnist/' # Path where MNIST files are located
weights_loaded = False

def nonlin(x,deriv=False):
            if(deriv==True):
                return x * (1-x)
            return 1/(1+np.exp(-x))

"""
Inspired by http://iamtrask.github.io/2015/07/12/basic-python-network/
"""
def train_mlp(X, y, hidden_neurons):
    global syn0
    global syn1
    global epochs
    global n
    global weights_loaded

    np.random.seed(1)

    print("Adding bias")

    #BIAS
    X = np.c_[X, -np.ones(DATA_SIZE)]
    print("Bias added")

    print("x: " + str(X.shape))
    print("y: " + str(y.shape))

    # randomly initialize our weights with mean 0
    if (not weights_loaded):
        syn0 = 2*np.random.random((len(X[0]), hidden_neurons)) - 1
        syn1 = 2*np.random.random((hidden_neurons + 1, len(y[0]))) - 1

    print("syn0: " + str(syn0.shape))
    print("syn1: " + str(syn1.shape))

    l0 = np.empty((1, 785))
    ly = np.empty((1, 10))

    epochs_1 = epochs - 1
    
    for j in range(DATA_SIZE):
        for k in range(epochs):
            
            for l in range(0, 785):
                l0[0][l] = X[j][l]

            for l in range(0, 10):
                ly[0][l] = y[j][l]

            l1 = nonlin(np.dot(l0, syn0))

            #Adding bias in hidden layer
            l1 = np.column_stack([l1, [-1]])
            l2 = nonlin(np.dot(l1, syn1))

            # how much did we miss the target value?
            l2_error = ly - l2

            # in what direction is the target value?
            # were we really sure? if so, don't change too much.
            l2_delta = n * l2_error*nonlin(l2,deriv=True)

            # how much did each l1 value contribute to the l2 error (according to the weights)?
            l1_error = l2_delta.dot(syn1.T)
            
            # in what direction is the target l1?
            # were we really sure? if so, don't change too much.
            l1_delta = l1_error * nonlin(l1,deriv=True)

            syn1 += l1.T.dot(l2_delta)

            l1_delta = l1_delta[:,:-1]

            syn0 += l0.T.dot(l1_delta)

            if (k == epochs_1):
                if (j % 100 == 0):
                    l_error = np.mean(np.abs(l2_error))
                    print (str(j) + " - " + str(k) + " - Error:" + str(l_error))
            elif (k % 100 == 0):
                l_error = np.mean(np.abs(l2_error))
                if (l_error < 0.015):
                    if (j % 100 == 0):
                        print ("breaked at " + str(j) + " - " + str(k) + " - Error:" + str(l_error))
                    break

def train_mnist():
    global hd

    labels, imgs = read_data("training", MNIST_PATH)

    images = np.empty([DATA_SIZE, 784])
    for i in range(0, DATA_SIZE):
        l = [None] * 784
        for j in range(0, 28):
            for k in range(0, 28):
                curr_pixel = imgs[i][j][k]
                if (curr_pixel < 128) :
                    l[j * 28 + k]= 0
                else:
                    l[j * 28 + k] = 1
        images[i] = l

    images_array = images #np.array(images)
    labels_array = np.empty([DATA_SIZE, 10])

    for i in range(0, DATA_SIZE):
    	l = np.zeros(10)
    	l[labels[i]] = 1
    	labels_array[i] = l

    train_mlp(images_array, labels_array, hd)

def save_weights():
    global syn0
    global syn1

    np.savetxt("mnist_syn0.txt", syn0, delimiter = ',')
    np.savetxt("mnist_syn1.txt", syn1, delimiter = ',')

def load_weights():
    global syn0
    global syn1
    global weights_loaded

    syn0 = np.loadtxt(open("mnist_syn0.txt", "rb"), delimiter=",", skiprows=0)
    syn1 = np.loadtxt(open("mnist_syn1.txt", "rb"), delimiter=",", skiprows=0)

    weights_loaded = True

#load_weights()
train_mnist()
save_weights()
