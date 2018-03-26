run:
	python3 mining_opinion_features.py
	python3 words_to_num.py
	cp num_phrase.txt CBA/num_phrase.txt
	cd CBA;\
	javac *.java;\
	java ClassCBA_App -Fnum_phrase.txt -N1 -C50 -S0.5 > output.txt
	cd ..;\
	pwd
	cp ./CBA/output.txt ./output.txt
	python3 num_to_words.py
	python3 compactness_pruning.py
	python3 redundancy_pruning.py
	python3 opinion_words_extraction.py > opinion_list.txt
	python3 orientation_identification_opinion_words.py
	python3 select_nouns.py
