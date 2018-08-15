## 关于Rasdaman

### 简介

**rasdaman（"raster data manager"）格数据管理器：** 号称是世界上最灵活和可伸缩的阵列引擎。

它允许存储和查询大量多维数组，如传感器、图像、模拟和统计数据，这些数据出现在地球、空间和生命科学等领域。这个全球领先的阵列分析引擎以其灵活性、性能和可扩展性而著称。rasdaman可以处理驻留在文件系统目录和数据库中的数组。

一个常用的同义词是栅格数据阵列，如二维光栅图形；这实际上促使名称rasdaman。然而，rasdaman在维度数目没有限制它可以，例如，一维二维测量数据、卫星图像、三维X/Y/T的时间序列图像和X、Y、Z的勘探资料，四维的海洋和气候数据，甚至超越时空的维度。

**新一代地理栅格服务器** ：从简单的地理图像服务到复杂的分析，rasdaman提供了时空光栅数据的所有功能——包括规则网格和不规则网格。正如最近的科学基准所显示的那样，它的性能和可扩展性是前所未有的。为了利用这种支持技术，用户不必学习新的接口:rasdaman与R、OpenLayers、Leaflet、NASA WorldWind、GDAL、MapServer、ESRI ArcGIS等软件进行了平滑集成，想要了解更多可点击[这里](http://rasdaman.org/wiki/Clients)。

更多关于rasdaman，请点击[这里](http://rasdaman.org/)。

### 安装

#### 创建rasdaman用户

```
yum install epel-release
adduser rasdaman
passwd rasdaman

# 切换到rasdaman用户
sudo -u rasdaman -i
```

**注：**  修改/etc/sudoers

##### 修改sudoers方法

```
vi /etc/sudoers

## 方法一： 把前面的注释（#）去掉
## Allows people in group wheel to run all commands
%wheel    ALL=(ALL)    ALL

## 然后修改用户，使其属于root组（wheel），命令如下：

#usermod -g root tommy


## 方法二：在root下面添加一行，如下所示：

## Allow root to run any commands anywhere
root    ALL=(ALL)     ALL
rasdaman   ALL=(ALL)     ALL
```

#### 需要包的说明和安装

```
sudo yum install \
  git make libtool pkgconfig m4 unzip curl \
  bison gcc gcc-c++ libedit-devel zlib-devel openssl-devel \
  flex flex-devel boost-devel libstdc++-static \
  gdal-devel hdf-devel netcdf-devel grib_api-devel netcdf-cxx-devel netcdf4-python \
  postgresql-devel postgresql-contrib postgresql-server sqlite-devel \
  gdal-python gdal-java python-setuptools python-pip python-magic python2-netcdf4 grib_api\
  java-1.8.0-openjdk java-1.8.0-openjdk-devel java-1.8.0-openjdk-headless tomcat maven2 \
  libgeotiff libgeotiff-devel libtiff libtiff-devel \
  doxygen
sudo yum install cmake3
sudo pip install glob2
```

**注**：源码编译安装boost,安装过程请参考[boost1.67](https://blog.csdn.net/xzwspy/article/details/81603227)，

#### 配置条件

1. 确保java和javac版本一致

  ```
  java -version
  javac -version
  ```

2. 允许用户添加部署tomcat webapps目录

  ```
  sudo adduser $USER tomcat
  # reboot or logout/login is necessary for this command to take effect
  ```

3. 如果支持PostgresSQL，请添加postgres用户：

  ```
  sudo -u postgres createuser -s $USER
  ```

4.  如果使用tomcat，请设置tomacat最大heap空间>=1GB。设置方式如下：

  修改etc/default/tomcat7的JAVA_OPTS=Xmx1024m，然重启tomcat `sudo service tomcat restart.`。如果安装的是8.0之后的tomcat，请修改安装目录下的`bin/catalina.sh`下修改如下代码：

  ```
  if [ -z "$JSSE_OPTS" ] ; then
  JSSE_OPTS="-Djdk.tls.ephemeralDHKeySize=2048"
  fi
  #JAVA_OPTS="$JAVA_OPTS $JSSE_OPTS"  #此处是注释
  JAVA_OPTS="-Xms512m -Xmx1024m"  #此处是新增
  ```

#### 获取源代码并安装

1. 获取源代码

  ```
  git clone https://github.com/javyxu/rasdaman.git # this creates subdirectory   rasdaman/
  cd rasdaman/
  ```

2. 编译并安装

  * 配置`～/.bashrc`文件

  ```
  export RMANHOME=/var/local/rasdaman
  export RMANSRC=/home/rasdaman/Downloads/rasdaman # rasdaman sources
  export RASDATA="$RMANHOME/data"
  export CATALINA_HOME=/var/lib/tomcat
  export PATH=$PATH:$RMANHOME/bin
  ```

  * 确保正确加载以上配置：`source ～/.bashrc`
  * 利用CMake（v3+）以上版本进行安装，本文安装的是cmake的版本是3.11.2，对应的boost版本是1.76，安装过程了解请点击[这里](https://blog.csdn.net/xzwspy/article/details/81603227).

  ```
  mkdir build
  cd build
  cmake3 ../rasdaman -DCMAKE_INSTALL_PREFIX=/var/local/rasdaman \
  -DFILE_DATA_DIR=/var/local/rasdaman/data -DDEFAULT_BASEDB=postgresql
  -DENABLE_PROFILING=ON -DGENERATE_DOCS=ON -DUSE_GRIB=ON -DUSE_NETCDF=ON \
  -DENABLE_BENCHMARK=ON
  make
  make install
  ```
  **cmake参数的数码： **

| 参数 | 可选 | 描述 |
| --- | ---- | --- |
| CMAKE_INSTALL_PREFIX | <path> (default /opt/rasdaman) | 安装目录 |
| CMAKE_BUILD_TYPE | Release,Debug (default Release) | 指定编译类型 |
| CMAKE_VERBOSE_OUTPUT | ON/**OFF** | 是否输出make的详细信息 |
| DEFAULT_BASEDB | **sqlite**/postgresql | 知道存储RASBASE的数据库 |
| ENABLE_BENCHMARK | ON/OFF | 生成输出的二进制文件 |
| ENABLE_PROFILING | ON/OFF | 是否用google-perftools进行分析查询 |
| ENABLE_DEBUG | ON/OFF | 生成可以调试/生成调试日志的二进制文件 |
| ENABLE_STRICT | ON/OFF | 在严格模式下启用编译(警告终止编译) |
| ENABLE_R | ON/OFF |是否支持R编译 |
| GENERATE_DOCS | ON/OFF | 生成安装文档 |
| GENERATE_PIC | ON/OFF | 生成位置独立的代码 |
| ENABLE_JAVA | ON/OFF | 生成和安装基于java的组件(rasj, petascope, secore) |
| JAVA_SERVER | external/embedded | 设置Java应用程序部署模式 |
| USE_GDAL | ON/OFF | 安装的时候是否包含GDAL |
| USE_GRIB | ON/OFF | 安装的时候是否包含GRIB |
| USE_HDF4 | ON/OFF | 安装的时候是否包含HDF4 |
| USE_NETCDF | ON/OFF | 安装的时候是否包含netCDF |
| FILE_DATA_DIR | <path>(default $RMANHOME/data) | 服务器存储切片文件地址 |
| WAR_DIR | <path>(default $RMANHOME/share/rasdaman/war) | Java war 文件被安装的路径 |

### 初始化数据库

1. 初始化rasdaman:

  ```
  create_db.sh
  ```

2. 启动rasdaman服务

  ```
  start_rasdaman.sh
  ```
3. 导入demo数据:

  ```
  rasdaman_insertdemo.sh localhost 7001 $RMANHOME/share/rasdaman/examples/images/ rasadmin rasadmin
  ```

4. 检查数据库是否正常:

   ```
   rasql -q 'select c from RAS_COLLECTIONNAMES as c' --out string
   ```

4. 停止/重启数据库

  ```
  stop_rasdaman.sh
  start_rasdaman.sh
  ```

### 初始化GEO服务

#### petascope

Petascope是rasdaman的geo Web服务前端。它在数组之上添加了geo语义，从而支持基于OGC覆盖标准的规则网格和不规则网格。

Petascope自动安装为rasdaman.war，除非指定了-DENABLE_JAVA=OFF cmake选项。所有war文件的部署目录可以在cmake选项设置`-dwarf _dir =<DIR>`;默认情况下，这是$RMANHOME/share/rasdaman/war。

安装petascope

  1. [在rasdaman下配置postgresql](http://rasdaman.org/wiki/rasdamanStoragePostgreSQL)

  2. 为petascope添加一个postgresql用户:

  ```
  sudo -u postgres createuser -s <username> -P
  > enter password
  ```

  3. 在`$RMANHOME/etc/petascope.properties `设置参数 `spring.datasource.username=petauser`/`spring.datasource.password=petapasswd` 和`metadata_user/metadata_pass`

  4. 确保PostgreSQL允许，拷贝rasdaman.war向tomcat目录下，并启动或者重启tomcat。

完全部署成功后，可以在http://localhost:8080/rasdaman/ows。


#### secore

SECORE(语义坐标引用系统解析器)是一个将CRS url映射到CRS定义的服务。这个组件是标准rasdaman分布的一部分，被[开放地理空间联盟](http://www.opengeospatial.org/)(OGC)用于运行他们的官方CRS解析器。Petascope使用SECORE来解析它所持有的覆盖率的CRS定义，如果SECORE作为`def.war`在本地部署，最好与Petascope的`rasdaman.war`一起部署。配置安装路径或禁用安装的方式与petascope相同。
