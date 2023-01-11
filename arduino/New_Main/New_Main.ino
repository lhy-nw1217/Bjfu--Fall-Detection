#include<MsTimer2.h>
#include <Wire.h>
//加速器传感器
#define ADXLAddress (0x53)     //ADXL345的I2C地址（ADDR接地）
//陀螺仪传感器ITG3205
#define ITGAddress   0x68    //ITG3205的I2C地址（AD0接地）
#define G_SMPLRT_DIV 0x15    //设置采样率的寄存器
#define G_DLPF_FS 0x16     //设置量程、低通滤波带宽、时钟频率的寄存器
#define G_INT_CFG 0x17     //设置中断的寄存器
#define G_PWR_MGM 0x3E    //设置电源管理的寄存器

int xGyro, yGyro, zGyro;      //存放角速度值,温度
int buffG[6];                  //存放寄存器高低位值，X、Y、Z轴共6个

int xAcc,  yAcc,  zAcc;       // 存放加速度值
int buffA[6];                  //存放寄存器高低位值，X、Y、Z轴共6个

// 陀螺仪传感器误差修正的偏移量
int g_offx = 0;
int g_offy = 0;
int g_offz = 0;

// 加速度传感器误差修正的偏移量
int a_offx = 0;
int a_offy = 0;
int a_offz = 0;

int flag = 0;//定时中断标志位

void timer(){
  flag = 1;
  }


void writeRegister(int deviceAddress, byte address, byte val)
{
  Wire.beginTransmission(deviceAddress);
  Wire.write(address);
  Wire.write(val);
  Wire.endTransmission();
}

void readRegisterA(int deviceAddress, byte address)
{
  Wire.beginTransmission(deviceAddress);
  Wire.write(address);
  Wire.endTransmission();
  Wire.beginTransmission(deviceAddress);
  Wire.requestFrom(deviceAddress, 6);

  int i = 0;
  while (Wire.available())
  {
    buffA[i++] = Wire.read();
  }
  Wire.endTransmission();
}


void readRegisterG(int deviceAddress, byte address)
{
  Wire.beginTransmission(deviceAddress);
  Wire.write(address);
  Wire.endTransmission();
  Wire.beginTransmission(deviceAddress);
  Wire.requestFrom(deviceAddress, 6);

  int i = 0;
  while (Wire.available())
  {
    buffG[i++] = Wire.read();
  }
  Wire.endTransmission();
}

void initGyro()
{
  /*****************************************
     ITG3205
     G_SMPLRT_DIV：采样率 = 125Hz
     G_DLPF_FS：+ - 2000度/秒、低通滤波器5HZ、内部采样率1kHz
     G_INT_CFG：没有中断
     G_PWR_MGM：电源管理设定：无复位、无睡眠模式、无待机模式、内部振荡器
   ******************************************/
  writeRegister(ITGAddress, G_SMPLRT_DIV, 0x07); //设置采样率
  writeRegister(ITGAddress, G_DLPF_FS, 0x1E); //设置量程、低通滤波带宽、内部采样率
  writeRegister(ITGAddress, G_INT_CFG, 0x00); //设置中断（默认值）
  writeRegister(ITGAddress, G_PWR_MGM, 0x00);    //设置电源管理（默认值）
}

void getGyroValues()
{
  readRegisterG(ITGAddress, 0x1D); //读取陀螺仪ITG3205的数据
  xGyro = ((buffG[0] << 8) | buffG[1]) + g_offx;
  yGyro = ((buffG[2] << 8) | buffG[3]) + g_offy;
  zGyro = ((buffG[4] << 8) | buffG[5]) + g_offz;
}



void initAcc() 
{
  //配置ADXL345，ADXL345采用默认的+-2g量程，10位分辨率
writeRegister (ADXLAddress, 0x2C, 0x09);//设置输出数据速率50Hz，带宽25Hz。
//默认值为0x0A,对应输出数据速率100Hz，带宽50Hz
writeRegister (ADXLAddress, 0x2D, 0x08);  //设置ADXL345为测量模式。
  }

void getAccData()
{
  readRegisterA(ADXLAddress, 0x32);  
  xAcc = ((buffA[1] << 8) | buffA[0] )+ a_offx;   
  yAcc = ((buffA[3] << 8) | buffA[2] )+ a_offy;
  zAcc = ((buffA[5] << 8) | buffA[4]) + a_offz;
}



void setup() {
  Serial.begin(9600);//和默认蓝牙一个通信波特率，保证信息传输正确
  Wire.begin();
  initAcc();
  initGyro();
  //100ms 一次检测，1s检测10次
  MsTimer2::set(100,timer);
  MsTimer2::start();
  //控制灯
  int i;
  for( i = 2 ; i < 6 ; i++ )
    {
        pinMode(i,OUTPUT);
        digitalWrite(i,HIGH);    // set led control pin defalut HIGH turn off all LED and 
    }
}

void loop() {
  
  if(flag){
    //加速度
    getAccData();
    Serial.print("xAcc=");
    Serial.print(xAcc,DEC);
    Serial.print("  yAcc=");
    Serial.print(yAcc,DEC);
    Serial.print("  zAcc=");
    Serial.println(zAcc,DEC);
    int a = sqrt((xAcc*xAcc)+(yAcc*yAcc)+(zAcc*zAcc));//计算合加速度
    double x = (atan(yAcc/sqrt(xAcc*xAcc+zAcc*zAcc))/PI);//翻滚角
    double y = (atan(-1*xAcc/sqrt(yAcc*yAcc+zAcc*zAcc))/PI);//俯仰角
    Serial.print(" A=");
    Serial.println(a,DEC);
    //陀螺仪
    getGyroValues();
    Serial.print("xGyro=");
    Serial.print(xGyro,DEC);
    Serial.print("  yGyro=");
    Serial.print(yGyro,DEC);
    Serial.print("  zGyro=");
    Serial.println(zGyro,DEC);

    delay(100);
    
  
    //阈值法,合加速度超过
    if(a>150){ 
      digitalWrite(5,LOW); //打开扬声器

      if(xGyro>yGyro && xGyro>zGyro) {digitalWrite(4,LOW);digitalWrite(3,HIGH);digitalWrite(2,HIGH); delay(500);flag = 0;};//4red
      if(yGyro>zGyro && yGyro>zGyro) {digitalWrite(3,LOW);digitalWrite(4,HIGH);digitalWrite(2,HIGH); delay(500);flag = 0;};//3orange
      if(zGyro>yGyro && zGyro>xGyro) {digitalWrite(2,LOW);digitalWrite(3,HIGH);digitalWrite(4,HIGH); delay(500);flag = 0;};//2blue
    }
    else{digitalWrite(2,HIGH);digitalWrite(3,HIGH);digitalWrite(4,HIGH);digitalWrite(5,HIGH);flag = 0;}
  }
   
}
