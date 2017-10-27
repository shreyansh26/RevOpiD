import ast
import os

path = './RAW_REVIEWS'
dirs = os.listdir(path)
path2 = './REVIEWS'

for File in dirs:
	#File = "HealthProd4.txt"
	print File
	I_file = open(path+'/'+File,'r')
	os.mkdir(path2+'/'+File[:-4])
	O_file = open(path2+'/'+File[:-4]+'/id_'+File,'w')
	for f in I_file:
		#print f
		d = ast.literal_eval(f)
		#print type(f)
		#print type(d)
		t = d['reviewerID']
		print t
		O_file.write(t)
		O_file.write('\n')
