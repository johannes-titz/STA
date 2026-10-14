gen2listBN <- function (data) {
  # converts a data frame into a list of lists of binomial data
  # suitable for staCMRBN.R
  #
groups = data[,1]; ugroup = unique(groups); ngroup = length(ugroup)
conds = data[,2]; ucond = if (is.factor(conds)) levels(droplevels(conds)) else sort(unique(conds)); ncond = length(ucond)
vars = data[,3]; uvar = unique(vars); nvar = length(uvar)
counts = data[,4:5]
output = vector("list", ngroup)

for (igroup in 1:ngroup) {
  temp = vector("list",nvar)
  for (ivar in 1:nvar) {
    k = which(groups==ugroup[igroup] & vars==uvar[ivar])
    aligned = match(ucond, as.character(conds[k]))
    if (anyNA(aligned) || anyDuplicated(conds[k])) stop("Every unit and outcome needs one count row per condition")
    temp[[ivar]] = counts[k[aligned],,drop=FALSE]
    names(temp[[ivar]]) = c("hits", "misses")
    rownames(temp[[ivar]]) = c(1:length(k))
    }
  output[[igroup]] = temp
} 
output$ngroup = ngroup; output$nvar = nvar; output$ncond = ncond
return (output)
}
