package org.banchen19.bc;

public class Box_Oj {
    Box_Item boxItem;

    public Box_Item getBoxItem() {
        return boxItem;
    }

    public void setBoxItem(Box_Item boxItem) {
        this.boxItem = boxItem;
    }

    //创建 SingleObject 的一个对象
    private static Box_Oj instance = new Box_Oj();

    //让构造函数为 private，这样该类就不会被实例化
    private Box_Oj(){}

    //获取唯一可用的对象
    public static Box_Oj getInstance(){
        return instance;
    }

}
