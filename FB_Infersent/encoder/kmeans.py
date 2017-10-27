from sklearn.cluster import KMeans

textVect = []
ind = 0
for i in reqdlabels:
	textVect.append(d2v_model.docvecs[i])

## K-means ##
num_clusters = 10
km = KMeans(n_clusters=num_clusters)
km.fit(textVect)
clusters = km.labels_.tolist()

## Print Sentence Clusters ##
cluster_info = {'sentence': reqddocs, 'cluster' : clusters}
sentenceDF = pd.DataFrame(cluster_info, index=[clusters], columns = ['sentence','cluster'])

for num in range(num_clusters):
	print "Sentence cluster: ", int(num+1),
	print()
	#print(sentenceDF.ix[num]['sentence'])
	for sentence in sentenceDF.ix[num]['sentence']:
		print sentence,
		print ''
	print ''
