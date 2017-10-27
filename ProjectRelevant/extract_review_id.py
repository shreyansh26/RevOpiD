import ast
import os

File = "0_AutomotiveProd3.txt"
print(File)
I_file = open(File,'r')
O_file = open('id_'+File,'w')
for f in I_file:
	print f
	d = ast.literal_eval(f)
	#print type(f)
	#print type(d)
	t = d['reviewerID']
	print t
	O_file.write(t)
	O_file.write('\n')
