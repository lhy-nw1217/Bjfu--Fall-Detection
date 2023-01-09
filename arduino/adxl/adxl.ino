
#include <Wire.h>
#define ADXLAddress (0x53)     //ADXL345的I2C地址（ADDR接地）
int xAcc,  yAcc,  zAcc;       // 存放加速度值
int buff[6];                  //存放寄存器高低位值，X、Y、Z轴共6个
 
// 加速度传感器误差修正的偏移量
int a_offx = 0;
int a_offy = 0;
int a_offz = 0;

void writeRegister(int deviceAddress, byte address, byte val)
 {
  Wire.beginTransmission(deviceAddress); 
  Wire.write(address);       
  Wire.write(val);        
  Wire.endTransmission();
}

void readRegister(int deviceAddress, byte address) 
{
  Wire.beginTransmission(deviceAddress);  
  Wire.write(address);        
  Wire.endTransmission(); 
  Wire.beginTransmission(deviceAddress); 
  Wire.requestFrom(deviceAddress, 6);   
 
  int i = 0;
  while(Wire.available())    
  {  buff[i++] = Wire.read();  }
  Wire.endTransmission(); 
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
  readRegister(ADXLAddress, 0x32);  
  xAcc = ((buff[1] << 8) | buff[0] )+ a_offx;   
  yAcc = ((buff[3] << 8) | buff[2] )+ a_offy;
  zAcc = ((buff[5] << 8) | buff[4]) + a_offz;
}

void setup()
{
  Serial.begin(14400);
  Wire.begin();
  initAcc();
  delay(50);
}
  
void loop()
{
    getAccData();
    Serial.print("xAcc=");
    Serial.print(xAcc);
    Serial.print("  yAcc=");
    Serial.print(yAcc);
    Serial.print("  zAcc=");
    Serial.println(zAcc);
   delay(200);
}
