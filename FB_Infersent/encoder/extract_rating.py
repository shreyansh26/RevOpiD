import ast
import os

File = "0_OfficeProd13.txt"
print(File)
I_file = open(File,'r')
O_file = open('rating_'+File,'w')
for f in I_file:
	print f
	d = ast.literal_eval(f)
	#print type(f)
	#print type(d)
	t = d['overall']
	print t
	O_file.write(str(t))
	O_file.write('\n')
