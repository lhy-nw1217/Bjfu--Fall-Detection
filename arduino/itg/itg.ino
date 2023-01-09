#include <Wire.h>
//陀螺仪传感器ITG3205
#define ITGAddress   0x68    //ITG3205的I2C地址（AD0接地）
#define G_SMPLRT_DIV 0x15    //设置采样率的寄存器
#define G_DLPF_FS 0x16     //设置量程、低通滤波带宽、时钟频率的寄存器
#define G_INT_CFG 0x17     //设置中断的寄存器
#define G_PWR_MGM 0x3E    //设置电源管理的寄存器

int xGyro, yGyro, zGyro;      //存放角速度值,温度
int buff[6];                  //存放寄存器高低位值，X、Y、Z轴共6个

// 陀螺仪传感器误差修正的偏移量
int g_offx = 0;
int g_offy = 0;
int g_offz = 0;

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
  while (Wire.available())
  {
    buff[i++] = Wire.read();
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
  readRegister(ITGAddress, 0x1D); //读取陀螺仪ITG3205的数据
  xGyro = ((buff[0] << 8) | buff[1]) + g_offx;
  yGyro = ((buff[2] << 8) | buff[3]) + g_offy;
  zGyro = ((buff[4] << 8) | buff[5]) + g_offz;
}

void setup()
{
  Serial.begin(19200);
  Wire.begin();
  initGyro();
  delay(50);
}

void loop()
{
  getGyroValues();
  Serial.print("xGyro=");
  Serial.print(xGyro);
  Serial.print("  yGyro=");
  Serial.print(yGyro);
  Serial.print("  zGyro=");
  Serial.println(zGyro);
  delay(200);
}
