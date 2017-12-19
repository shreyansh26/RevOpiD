import os
import shutil
import errno

DIR = './DEVELOPMENT_DATA_OPINION_MATRICES'
DIR2 = './RESULTS'

def copy(src, dest):
    try:
        shutil.copytree(src, dest)
    except OSError as e:
        # If the error was caused because the source wasn't a directory
        if e.errno == errno.ENOTDIR:
            shutil.copy(src, dest)
        else:
            print('Directory not copied. Error: %s' % e)

dirs = os.listdir(DIR)

for dir in dirs:
	for file in os.listdir(DIR+'/'+dir):
		if file.endswith('_two.csv'):
			copy(DIR+'/'+dir+'/'+file, DIR2+'/matrix_'+dir+'.csv')
