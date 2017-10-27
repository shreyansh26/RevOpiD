from nltk.stem import WordNetLemmatizer
from nltk.corpus import stopwords
from nltk import word_tokenize, pos_tag
import string, re
from autocorrect import spell
from nltk.tokenize import TweetTokenizer
from nltk.stem.lancaster import LancasterStemmer
from nltk.corpus import state_union
import nltk

lancaster_stemmer = LancasterStemmer()
tknzr = TweetTokenizer()  # This helps maintain apostrophe like "hasn't"
lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words("english"))


PROD = 'AutomotiveProd3'
f = 'text_0_'+PROD+'.txt'
#f = '0_'+PROD+'.txt'
f = open(f)

RE_D = re.compile('\d')
RE_AP = re.compile("'")
RE_DO = re.compile("\.")

limit = 0
i = 0
r = []
for l in f:
	i+=1
	if(i==limit):
		break
	if(i%2==0):
		continue
	r.append(l[:-1])
f.close()

#print(r[2])

for idx,r1 in enumerate(r):
	r1 = r1.lower()
	r[idx] = tknzr.tokenize(r1)   # or word_tokenize(r1)
	for i in range(len(r[idx])):
		if ((r[idx][i] not in set(string.punctuation)) and (not (RE_D.search(r[idx][i]))) and (not (RE_AP.search(r[idx][i]))) and (not (RE_DO.search(r[idx][i]))) and (r[idx][i] != 'was') and (r[idx][i] != 'has')):
			r[idx][i] = spell(r[idx][i])
		if r[idx][i] in stop_words:
			r[idx][i] = ''
		r[idx][i] = lemmatizer.lemmatize(r[idx][i])  # or lancaster_stemmer.stem(r[idx][i]) but changes the word a lot

	r[idx] = (' ').join(r[idx])
	r[idx] = r[idx].strip()
	r[idx] = r[idx].lower()
	r[idx] = re.sub('\s+', ' ', r[idx])
	#r[idx] = r[idx].translate(str.maketrans('','',string.punctuation))

#print(r[0].split())
f = open('cleaned_data2.txt', 'w')
for i in r:
	f.write(i)
	f.write('\n')

f.close()

r = []
f = open('cleaned_data2.txt', 'r')
for l in f:
	r.append(l[:-1])
f.close()

#print(r[0])

chunkList = []
for idx,r1 in enumerate(r):

	r2 = r1.lower()
	for r1 in r2.split('.'):
		r3 = tknzr.tokenize(r1)    # or word_tokenize(r1) tknzr.tokenize
		tagged = nltk.pos_tag(r3)
		chunkGram = "NP: {<DT>?<JJ>+<NN>}"
		chunkParser = nltk.RegexpParser(chunkGram)

		chunked = chunkParser.parse(tagged)
		chunkList.append(chunked)

#print(chunkList[0])
noun_phrase_list = []
for i in chunkList:
	for subtree in i.subtrees():
			if subtree.label() == 'NP':
				np_phrase = []
				for idx, x in enumerate(subtree.leaves()):
					if (x[0] == '"' or x[0] in '\/'):
						continue
					np_phrase.append(x[0])
				noun_phrase_list.append(np_phrase)

f = open('noun_phrase2.txt', 'w')

for i in noun_phrase_list:
	f.write(' '.join(i))
	f.write('\n')
f.close()