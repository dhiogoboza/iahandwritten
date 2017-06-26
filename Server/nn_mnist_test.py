import PIL
from PIL import Image
import numpy as np

import ntpath
import os
import sys
import time

from mnistparser import read_data

import pygame
import pygame.camera

n = 0.001 # 0.0005
syn0 = 0
syn1 = 0
hd = 128 #180
epochs = 1000
DATA_SIZE = 60000
MNIST_PATH = 'python-mnist/'

def nonlin(x,deriv=False):
            if(deriv==True):
                return x * (1-x)
            return 1/(1+np.exp(-x))

def threshold_pixels(data, height=28, width=28):
    local_x = np.empty( (height * width)+ 1)

    for y in range(height):
        for x in range(width):
            i = (y * width) + x
            
            if (data[i] < 128) :
                local_x[i]= 1
            else:
                local_x[i] = 0
    local_x[height * width] = -1

    return local_x

def crop_images(image_path, crop_type, target_folder, cols, rows, image_width, image_height):
    img = Image.open(image_path)

    width = image_width / cols
    height = image_height / rows

    images = []

    for i in range(0, rows):
        print("i: " + str(i))
        for j in range(0, cols):
            if crop_type == 1:
                start_x = j * width + 2
                start_y = i * height + 2
                box = (int(start_x),  int(start_y), int(23 + start_x), int(23 + start_y))
            elif crop_type == 2:
                start_x = j * width
                start_y = i * height
                box = (int(start_x),  int(start_y), int(25 + start_x), int(25 + start_y))

            crop = img.crop(box)  
            crop.load() 
            temp_name = target_folder + "/temp_" + str(i) + "-" + str(j) + ".bmp"
            image_name = target_folder + "/crop_" + str(i) + "-" + str(j) + ".bmp"

            crop.save(temp_name, "bmp")

            size = 28, 28
            im = Image.open(temp_name).convert('RGB')
            im = im.resize(size, PIL.Image.ANTIALIAS)
            im = threshold(im, size[0], size[1])
            im.save(image_name, "bmp")

            images.append(image_name)

    return images

def get_number(output_array):
    global max_index

    max_index = 0
    for i in range(0, len(output_array)):
        if (output_array[i] > output_array[max_index]):
            max_index = i

    return [max_index, output_array[max_index]]

def resolve_image(image_path):
    img = Image.open(image_path)
    rgb_im = img.convert('RGB')
    pixels = list(rgb_im.getdata())

    max_len = 28 * 28

    for j in range(len(pixels), max_len):
        pixels.append((255, 255, 255))

    local_x = np.empty(max_len + 1)

    for i in range(0, len(pixels)):
        if ((sum(pixels[i])/len(pixels[i])) < 128):
          local_x[i] = 1
        else:
          local_x[i] = 0

    local_x[max_len] = -1

    for i in range(0, 28):
      linestr = " "
      for j in range(0, 28):
        linestr += str(int(local_x[i * 28 + j])) + " "
      print(linestr)

    l0 = local_x
    l1 = nonlin(np.dot(l0, syn0))
    l1 = np.concatenate(([l1, [-1]]))

    l2 = nonlin(np.dot(l1, syn1))

    print("l2: " + str(l2))

    result = get_number(l2)

    return result

def do_matrices_tests(tests, answer):
    hits = 0
    total = 0

    tests = np.c_[tests, -np.ones(len(tests))]

    for i in range(0, len(tests)):
        l0 = tests[i]
        l1 = nonlin(np.dot(l0, syn0))

        l1 = np.concatenate(([l1, [-1]]))

        l2 = nonlin(np.dot(l1, syn1))

        result = get_number(l2)

        if (result[0] == answer[total]):
            hits += 1
            print(str(i) + "= " + str(result) + " SUCCESS")
        else:
            print(str(i) + "= " + str(result) + " FAIL")
        
        total += 1

    print("Porcentagem de acertos: " + str(100 * hits/total))

def do_tests(tests, answer):
    hits = 0
    total = 0

    test_images = []
    
    for test in tests:
        img = Image.open(test).convert('L')
        pixels = list(img.getdata())


        local_x = threshold_pixels(pixels)

        test_images.append(local_x)

        l0 = local_x
        l1 = nonlin(np.dot(l0, syn0))
        # hidden layer with bias
        l1 = np.concatenate(([l1, [-1]]))
        l2 = nonlin(np.dot(l1, syn1))

        result = get_number(l2)

        if (not answer is None):
            if (result[0] == answer[total]):
                hits += 1
                print(test + "= " + str(result) + " SUCCESS")
            else:
                print(test + "= " + str(result) + " FAIL")
        
        total += 1

    print("Porcentagem de acertos: " + str(100 * hits/total))

    return test_images

def test_mnist():
    global hd

    labels, imgs = read_data("testing", "python-mnist/data/")

    DATA_SIZE = len(imgs)

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

    print("images: " + str(images.shape))
    print("images: " + str(len(images)))
    print("labels: " + str(len(labels)))

    images_array = images

    np.savetxt("images_array_test.txt", images_array, delimiter = ',')
    np.savetxt("labels_array_test.txt", labels, delimiter = ',')

    do_matrices_tests(images_array, labels)

def test_mnist2():
    global hd

    images_array = np.loadtxt(open("images_array_test.txt", "rb"), delimiter=",", skiprows=0)
    labels_array = np.loadtxt(open("labels_array_test.txt", "rb"), delimiter=",", skiprows=0)

    do_matrices_tests(images_array, labels_array)

def execute_tests(dotests):
    tests = crop_images("sample_tests/all_numbers.png", 2, "all", 20, 10, 530, 297)

    tests_answer = [
    	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
    	2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
    	3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,
    	4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
    	5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
    	6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    	7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    	8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
    	9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9
    ]

    tests_answer_a = []
    testst_images_matrices = None

    testst_images_matrices = do_tests(tests, tests_answer)

    return testst_images_matrices, tests_answer_a

def load_weights():
    global syn0
    global syn1

    syn0 = np.loadtxt(open("mnist_syn0.txt", "rb"), delimiter=",", skiprows=0)
    syn1 = np.loadtxt(open("mnist_syn1.txt", "rb"), delimiter=",", skiprows=0)

def test_cam_image():
    cap_img_name = "camtests/capture.bmp"
    test_img_name = "camtests/test.bmp"

    pygame.camera.init()
    pygame.camera.list_cameras() #Camera detected or not
    cam = pygame.camera.Camera("/dev/video0",(640,480))
    cam.start()

    while (1):
        img = cam.get_image()
        pygame.image.save(img, cap_img_name)

        size = 28, 28
        im = Image.open(cap_img_name)
        #im = im.thumbnail(size, Image.ANTIALIAS)
        im = im.resize(size, PIL.Image.ANTIALIAS)
        im.save(test_img_name, "bmp")

        result = resolve_image(test_img_name)

        print("result: " + str(result))
        time.sleep( 3)

def threshold(image, height, width):
    data = np.asarray(image, np.uint8)

    local_x = np.empty( (height, width, 3) , np.uint8)

    for y in range(height):
        for x in range(width):
            curr_pixel = (data[y][x][0] / 3) + (data[y][x][1] / 3) + (data[y][x][2] / 3)
            
            if (curr_pixel < 128) :
                local_x[y][x]= (0, 0, 0)
            else:
                local_x[y][x] = (255, 255, 255)

    return Image.fromarray(local_x)

def test_single_image(image_path):
    temp_image = "temp/" + str(ntpath.basename(image_path))

    im = Image.open(image_path)
    width, height = im.size
    
    im = threshold(im, height, width)
    size = 28, 28
    im = im.resize(size, PIL.Image.ANTIALIAS)
    im.save(temp_image, "bmp")

    result = resolve_image(temp_image)

    print("\nresult: " + str(result))


load_weights()


if (len(sys.argv) > 1):
    if (not os.path.exists("temp/")):
        os.mkdir("temp/")
    if (sys.argv[1] == "-i"):
        test_single_image(sys.argv[2])
    elif (sys.argv[1] == "-mnist"):
        test_mnist()
    elif (sys.argv[1] == "-camera"):
        test_cam_image()
    elif (sys.argv[1] == "-tests"):
        execute_tests()
else:
    print("Usage nn_mnist.py [-i input_image] | [-camera] | [-tests]")


#test_cam_image()

#test_single_image("1_test.bmp")#, 150, 150)
#print("\n")
#test_single_image("4_test.bmp")#, 200, 200)
#print("\n")
#test_single_image("5_test.bmp")#, 260, 260)

#execute_tests1()
#execute_tests2(True)
#test_mnist()































