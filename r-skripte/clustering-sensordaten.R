x = read.table("~/sensor.log")

data =x$V1
num_larger_01 = length(which(data>0.1))#kleiner 5%