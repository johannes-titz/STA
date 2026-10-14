# Run from STACMR-R with its built fxMR-0.3.45 JAR and R dependencies available.
source("staCMRsetup.R")
source("Utility functions/shrinkDiag.R")
stopifnot(abs(shrinkDiag(c(1,2,3))$sigma[1,1]-2/3)<1e-12)
stopifnot(abs(shrinkDiag(c(1,NA,2,3))$sigma[1,1]-2/3)<1e-12)

stats <- list(list(means=c(2,1),weights=diag(8,2),shrinkage=0),
              list(means=c(2,0),weights=diag(8,2),shrinkage=0))
for(approx in c(FALSE,TRUE)) {
  fit <- staCMRx(stats,E=list(1:2),proc=2,approx=approx)
  stopifnot(abs(fit$fval-20)<1e-7,all(vapply(fit$x,function(x)x[1]<=x[2]+1e-8,TRUE)))
}
fit <- staCMRx(stats,model=diag(2),E=list(1:2),proc=2)
stopifnot(abs(fit$fval-20)<1e-7)

y <- lapply(1:2,function(g) lapply(1:3,function(v)
  matrix(sin(seq_len(24)*(g+v))+rep(c(3,1,2),each=8),8,3)))
general <- do.call(rbind,lapply(1:2,function(g) do.call(rbind,lapply(1:3,function(v)
  data.frame(id=1:8,group=g,dv=v,y[[g]][[v]])))))
for(input in list(y,general)) {
  fit <- jMRfits(3,input,list(1:3,4:6),shrink=1,proc=1,seed=2018)
  stopifnot(length(fit$pars)==3,identical(dim(.jevalArray(fit$pars[[1]],simplify=TRUE)),c(3L,6L)))
}
fit <- staMRFIT(y,list(1:3,4:6),nsample=1,shrink=1)
stopifnot(all(vapply(fit$pars,function(x)identical(dim(x),c(1L,6L)),TRUE)))
stopifnot(identical(dim(gen2list(data.frame(id=1,group=1,dv=1,x=2))[[1]][[1]]),c(1L,1L)))

counts <- list(list(cbind(c(0,10),c(10,0)),cbind(c(0,10),c(10,0))))
mr <- staMRBN(counts)
cmr <- staCMRBN(counts)
stopifnot(mr$g.squared[1]==0,cmr$g.squared[1]==0)
shuffled <- data.frame(unit=1,condition=c(2,1,2,1),dv=c(1,1,2,2),hits=c(10,0,10,0),misses=c(0,10,0,10))
aligned <- gen2listBN(shuffled)
stopifnot(all(as.matrix(aligned[[1]][[1]])==counts[[1]][[1]]))
fit <- staMRFITBN(shuffled,nsample=1,proc=1,seed=2018)
stopifnot(all(vapply(fit$pars[[1]],function(x)identical(dim(x),c(1L,2L)),TRUE)))
cat("Standalone R correctness regressions passed.\n")
