package com.example.demo;

public class JDK17Example {
    public static void main(String[] args) {
//        switchCase();
//        stringBlock();
//        instanceOfCase();
//        sealedClassCase();
//        recordCase();
//        nullInfoCase();
        System.out.println(new JDK17Example().qqId());
    }

    private static void switchCase() {
        var name = "徐庶";
        String alias = switch (name) {
//            case null -> "what"; // 预览版功能
            case "周瑜", "周瑜1" -> {
                System.out.println("这是捅了周瑜窝了");
                yield "公瑾";
            }
            case "徐庶" -> "元直";
            default -> "未知";
        };
        System.out.printf("测试swith：" + alias);
    }

    private static void stringBlock() {
        System.out.print("\n");
        // 提供了两个转义符，\用来将两行连为一行
        // \s用来设置单个空白字符

        String s1 = """
                This is a simple 
                text block example.
                """;
        System.out.println(s1);

        String s11 = """
                This is a simple \
                text block example\s.
                """;
        System.out.println(s11);

        String s2 = """
                {"a": 1, "b": 2}
                """;
        System.out.println(s2);

        String s3 = """
                <xml>
                    <body>hello, world</body>
                </xml>
                """;
        System.out.println(s3);
    }

    private static void instanceOfCase() {
        Object o = 1;
        if (o instanceof Integer i) {
            System.out.println("instanceof Integer: " + i);
        } else {
            System.out.println("instanceof others type: " + o);
        }
    }

    // sealed class
    static sealed class Father permits Son, Daughter {

    }

    static final class Son extends Father {
        private UserRecord userRecord;
        public UserRecord getUserRecord() {
            return userRecord;
        }
    }

    static non-sealed class Daughter extends Father {
    }

    private static void sealedClassCase() {
        Son son = new Son();
        System.out.println("sealedClass son: " + son);
    }

    // record, 提供全参构造函数 + getter
    record UserRecord(Long id, String name){}
    private static void recordCase() {
        UserRecord userRecord = new UserRecord(1L, "bruce");
        System.out.println("record: " + userRecord);
        System.out.println("record.name: " + userRecord.name());
    }

    // null 空指针报错详细信息
    private static void nullInfoCase() {
        Son son = new Son();
        System.out.println("null exp:" + son.getUserRecord().name());;
    }

    long qqId() {
        return 10L;
    }

}
