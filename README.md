## Synopsis

MetaDiff is a Java/R-based software package that performs differential expression analysis on RNA-Seq based data.
By utilizing a meta-regression framework, it is able to take advantage of the information regarding the variance of the estimates to make the inference more accurate.
Meta-regression also enables incorporation of covariates other than experimental group, which makes it extremely simple to adjust for confounding parameters in an experiment.

## Motivation

When performing a post hoc differential expression analysis, the currently available packages only take into account the counts or FPKM output generated by the upstream software, discarding the variance information.
This will often inflate the alpha-values, and lead to false positives or overly conservative inference. Meta-regression models address this issue by directly utilizing the variance output from upstream software, and

## Dependency

This program can only be run in a Unix-like operating system.
Tested on Ubuntu Linux 14.04 LTS, Red Hat Enterprise Linux Server release 6.6 (Santiago), and OS X Yosemite on my own MacBook Air.

This program requires:

1. R:
http://cran.r-project.org/index.html

2. Java Runtime SE 7/8
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

3. R packages:
doParallel, metatest, plyr

Make sure Rscript command is in your PATH variable.


## Installation

A compiled JAR package is ready for download at:
http://github.com/jiach/MetaDiff/blob/master/out/artifacts/MetaDiff_jar/MetaDiff.jar?raw=true

Download this JAR file to any location to your liking (for example /home/mason/myfavoriaternaseqprograms/MetaDiff.jar), and invoke the command as described in Usage.

## Usage

`java -jar /home/mason/myfavoriaternaseqprograms/MetaDiff.jar -input_file_list input_list.tsv -output_dir /home/mason/metadiff_results/ -method cufflinks`

These three parameters are required:

input_file_list - this is a tab-delimited file that contains the file locations and covariate information separated by tabs with header line. Here's an example:

| Sample 	| File_Name                                                                   	| C_group 	| age 	| C_gender 	|
|--------	|-----------------------------------------------------------------------------	|---------	|-----	|----------	|
| simu1  	| /home/mason/awesomeRNASeqData/simu1/isoforms.fpkm_tracking 	| 0       	| 40  	| female   	|
| simu2  	| /home/mason/awesomeRNASeqData/simu2/isoforms.fpkm_tracking 	| 0       	| 38  	| male     	|
| simu3  	| /home/mason/awesomeRNASeqData/simu3/isoforms.fpkm_tracking 	| 0       	| 56  	| male     	|
| simu4  	| /home/mason/awesomeRNASeqData/simu4/isoforms.fpkm_tracking 	| 1       	| 52  	| male     	|
| simu5  	| /home/mason/awesomeRNASeqData/simu5/isoforms.fpkm_tracking 	| 1       	| 47  	| male     	|
| simu6  	| /home/mason/awesomeRNASeqData/simu6/isoforms.fpkm_tracking 	| 1       	| 41  	|          	|

The first two columns indicate the sample_id and the file_names associated with that sample_id. Sample_ID can be any unique identifier of your sample (no two sample IDs should be the same). 
Use absolute path (path starting from root /) in File_name to avoid confusion of the program.

There can be any number of covariates in any order. Names of the covariates need to following the conventions of R variable names. The rule of thumb is to use letters, underscore and numbers, and to start the name with a letter.
Categorical covariates NEED to be prefixed with C_ in their names.

The C_group variable is the default variable name for the experimental group. The program will look specifically for this covariate to determine the group to which each subject belongs. 
If you need cv_threshold adjustment, this variable has to be present in the list file, and needs to be named C_group.

output_dir - again, use absolute path to avoid confusion.

method - indicates which software package generated the files in the input_file_list, as of right now the program supports 'cufflinks' and 'mmseq'.


Some optional parameters can also be provided:

cv_threshold - the program automatically removes samples with coefficient of variations from all groups >1. However, if you need further filtering with regards to CV, you can provide your cut off here.
The program will label the samples with CVs from ALL groups > cv_threshold as STATUS = FAILED in the output.

mean_fpkm_threshold - the program by default removes samples with the average FPKM from all groups <= 1. If you want to change this parameter, you can provide your own cut-off here.

num_cores - the program supports parallel computing with plyr and doParallel package in R. This parameter indicates the number of concurrent processes you wish to run when performing meta-regression with R. By default the program will only run 1 process.
If you have a multi-core system, I would highly recommend enabling this option by providing the number of cores you wish to use, because it will dramatically increase the speed of the program.

verbose - print the messages on screen in addition to writing it to log files. 

## Output

The program will generate the following files in the output_dir provided:
metadiff_results.tsv - a tab-delimited file containing the results of meta-regression. Usually it will be made of these columns:
Isoform - names of the isoform or gene (the name of this column will be changed to "Feature_ID" in future updates);
Convergence - convergence status output by metatest;
tval_CovariateName - t-test statistics for each covariate;
dfttest - degrees of freedom for the t-test;
pttest_CovariateName - t-test p-values for each covariate;
bartLLR_CovariateName - Bartlett corrected LLR statistics for each covariate;
pBartlett_CovariateName - Bartlett corrected LLR p-value for each covariate;
Status - OK indicates that the model has converged and the CV of FPKM for all groups are <= cv_threshold


Some temporary files are also created:
fpkm.mat - temporary files containing all the fpkm values, their variances and the covariates of the corresponding sample;
run_metatest.R - R script to perform metatest;
metadiff.log - log file containing all the operations performed by MetaDiff;
metadiff_r.log - standard error output from the R script;

## License

Copyright (c) <2015> <Cheng Jia>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
