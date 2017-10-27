import ast
import os

path = './RAW_REVIEWS'
dirs = os.listdir(path)
path2 = './TEXT_REVIEWS'
for File in dirs:
	#File = "HealthProd4.txt"
	print File
	I_file = open(path+'/'+File,'r')
	O_file = open(path2+'/'+'text_'+File,'w')
	for f in I_file:
		#print f
		d = ast.literal_eval(f)
		#print type(f)
		#print type(d)
		t = d['reviewText']
		print t
		O_file.write(t)
		O_file.write('\n\n')
