all:
	./makeShimpleOptions.awk options > ../ShimpleOptions.java
	./makeOptDoc.awk options > opts.tex
	latex opts.tex
	latex2html -no_auto_link -no_subdir -split 0 opts.tex
	dvips -o opts.ps opts.dvi
	/bin/rm labels.pl opts.aux opts.dvi opts.log opts.toc
