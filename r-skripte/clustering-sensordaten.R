library(rattle)
library(rpart.plot)
library(RColorBrewer)

x = read.table("~/sensor.log")

data =x$V1
num_larger_01 = length(which(data>0.1))#kleiner 5%
require(fpc)
res = dbscan(data,eps=0.01,MinPts = 3)
#Ein sehr großes Cluster, 6 sehr kleine
#2 Cluster scheinen gut zu funktionieren

res_kmeans = kmeans(data,3)
res_kmeans$centers
#kmeans mit 3; 4 Datenpunkte deren Fehler
df=data.frame(cbind(data=data,clust=factor(res_kmeans$cluster), clust_center = res_kmeans$centers[res_kmeans$cluster]))
err = (df$data - df$clust_center)**2
rpart_fit =rpart(clust~data,data=df,method = "class")
fancyRpartPlot(rpart_fit)

df=data.frame(data,clust_center = x$centers[x$cluster])



