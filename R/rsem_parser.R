args <- commandArgs(trailingOnly = TRUE)
library(EBSeq)
rsem_list <- as.data.frame(read.table(file = args[1], header = F, sep = "\t"))



IsoMat <- NULL

if (tolower(args[2])=='genes') {
  rsem_fn <- paste(as.character(rsem_list[,1]),".genes.results",sep='')
} else if (tolower(args[2])=='isoforms') {
  rsem_fn <- paste(as.character(rsem_list[,1]),".isoforms.results",sep='')
  
  file_one <- as.data.frame(read.table(rsem_fn[1],header = T,sep = "\t"))
  
  IsoNames = as.character(file_one[,1])
  IsosGeneNames = as.character(file_one[,2])
  NgList=GetNg(IsoNames, IsosGeneNames)
  
  for (idx in c(1:length(rsem_fn))){
    print(idx)
    IsoMat <- cbind(IsoMat, read.table(rsem_fn[idx],header = T,sep = "\t")[[5]])
  }
  dimnames(IsoMat)[[1]] <- IsoNames
  IsoSizes=MedianNorm(IsoMat)
  save('IsoNames', 'IsosGeneNames', 'NgList', 'IsoMat', 'IsoSizes', file = paste(dirname(args[1]),'/ebseq_input.RData',sep=''))
  IsoEBOut=EBTest(Data = IsoMat, NgVector = NgList$IsoformNgTrun, Conditions = as.factor(rsem_list[,2]), sizeFactors = IsoSizes, maxround = 5)
  IsoPP=GetPPMat(IsoEBOut)
  IsoDE=rownames(IsoPP)[which(IsoPP[,"PPDE"]>=.95)]
  
} else {
  print('Exiting because the second argument is neither "genes" or "isoforms"!')
  q()
}

save('IsoEBOut', 'IsoPP', 'IsoDE', file = paste(dirname(args[1]),'/ebseq_output.RData',sep=''))