
## **Android_Facenet_Recognition**

参考代码：

- https://github.com/vcvycy/Android_Facenet
- https://github.com/jiangdongguo/OpenCV4Android

增加了 初始化(提取特征得到特征库)
增加了 录入照片功能
增加了 实时识别功能

开发平台: rk3399开发板，Android 7.1.2，
IDE:  Android Studio 3.1.2

由于模型没有做压缩速度偏慢，模型也是用到其他人训练好的，所以准确率感人。。

人脸检测用到opencv 直接用的检测demo，得到脸部位置，送给facenet提取特征，于特征库比较（欧式距离），得到相似度。

录入功能用到mtcnn捕捉人脸后，存入脸部照片，其实可以增加一个输入照片信息的框和得到照片提取照片存入特征库。

后续工作 优化识别的速度，模型压缩，增加活体识别。

导师是想让我用ncnn或者tengine去实现，但是不会弄就用了tensorflow lite。








![2019_05_23_06.08.16](/2019_05_23_06.08.16.jpg)
