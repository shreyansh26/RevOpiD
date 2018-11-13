Review Opinion Diversification
==============================

A project I worked on in my 3rd Semester. [RevOpiD](https://sites.google.com/itbhu.ac.in/revopid-2017) was a shared task in IJCNLP 2017, Taipei, Taiwan.

I implemented three approaches of solving the problem -  
1. Opinion Feature Mining (**Mining Opinion Features in Customer Reviews** (Liu et al.))
2. Doc2Vec Model and Clustering
3. Facebook Research's [InferSent](https://github.com/facebookresearch/InferSent) model for sebtence embeddings with Clustering

Three clustering techniques were tried -
* K-Means Clustering
* Spectral Clustering
* Agglomerative Clustering

Of the three, Spectral Clustering gave the best results on manually checking the contents of the clusters. So in all the description of the models, clustering implies Spectral Clustering.

This project was done under the guidance of [Dr. A.K. Singh](https://www.iitbhu.ac.in/dept/cse/people/aksinghcse), Associate Professor, Department of Computer Science and Engineering, IIT (BHU) Varanasi.

Also, special thanks to [Avijit Thawani](https://avi-jit.github.io/) for his mentorship and the intersting ideas he provided to solve the problems I faced in the way. 
