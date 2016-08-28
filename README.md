# BLE_BluetoothDemo
Android低功耗蓝4.0牙开发示例

问题发现：
    该蓝牙一共有三个服务service，每个service有不同的特征 characteristic, 而每个特征具有不同的属性（property）;

    NRF51822 服务及特性如下：

    Service1:
        cha1 ---- read & write
        cha2 ---- read
        cha3 ---- read

    Service2:
        cha4 ---- indicate

    Service3:
        cha5 ---- notify
        cha6 ---- write & write no notify

    我们此处发送数据用的是 cha6特征, 而它是不能接收通知的（也就是不能接收蓝牙返回给手机端的数据)， 而cha5 则是掌控能否接受通知的特性；
    因此，我们需要通过： mBluetoothGatt.setCharacteristicNotification(cha5, true);  开启 cha5 接收通知的操作，才可以成功接受到蓝牙返回的数据;
    值得注意的是，每个不同的特征是通过各自唯一的 UUID 来区分；


