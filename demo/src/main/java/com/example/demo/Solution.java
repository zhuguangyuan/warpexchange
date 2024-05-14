package com.example.demo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * 需求
 * 展示形式
 * 母账号 下面有多个子账号  // 多个账号
 *      账号列表
 *          # ak sk 管理(加密展示)
 *          # balance freeze(仅展示)
 *          # 变动提示
 *              - 波动超过20%
 *                  - 时间段波动 可配置
 *              ## 不足提示/大额交易提示
 *              ## 二期 threshold预警值(可配置)
 *              ## 二期 日交易量、交易量变化趋势
 *
 *          ### -- 充值 提现 todo
 *              ### 充值提现订单 同步到本地
 *
 * 工程实现
 * - api
 *  - binance接口文档
 * - 后端架构
 *  - 数据存储
 *      - 【账户秘钥表】 ak sk 加密存储 加密算法 非对称加密RSA
 *      - 【账户余额表】 随时间变动插入新记录(余额快照)
 *      - 【告警配置表】 时间段-告警阈值-币种
 *  - java服务
 *      - REST
 *          - 账户的增删改查(ak sk 查询）
 *          - 告警配置接口
 *      - ws 余额变动实时监控 余额变动20%告警
*       - 手机短信 钉钉 企业微信
*              - todo 有哪些比较成熟的解决方案
*              - 短信服务接口
 *      - 告警触发后台服务
 *          - 读取配置表 并缓存 注意通过接口改动后更新缓存
 *              - 启动定时器
 *          - 实现方案
     *          - 后台线程循环计算1s
     *              - 多个滑动窗口 1min 5%  10min 30%
 *              - 在binance ws 回来的节点
 *                  - 计算每个触发器  1min 5%  10min 30% 和前一个位点的波动值，判断是否触发
 *                  - 可能问题
 *                      - ws断联
 *                      - 每个账户一个ws连接
 *                      - 解决方式：api 获取余额
 *                   - 配置定时器来触发
 *                      - 启动时间？服务起来后
 *                          - 触发时间点问题
 *                          corn(xxl quartz持久化) 分布式定时任务
 *          - 1min 5%
 *          - 2S 100BTC  3s 20BTC 60S 97BTC
 *              - 1min
 *              - 2min
 *              - 3min
 *
 *
 *
 * - 前端架构
 *  -
 *  - TODO
 *
 *
 *
 *  给一个x、y矩阵，如果x的上下或左右相邻，那么我们把这些相邻的x归为一个块，判断一个矩阵中块的个数。
 * 例如：
 * 输入
 * [
 * [x,x,y,y,x],
 * [y,y,y,x,x],
 * [x,x,y,x,y],
 * [y,y,y,y,y],
 * [x,x,x,y,y]
 * ]
 *
 * 注：存储的xy其实是字符'x'、'y'
 *
 * 输出：4
 *
 */
public class Solution {
    public static void main(String[] args) {
        char[][] arr = {
                {'x','x','y','y','x'},
                {'y','y','y','x','x'},
                {'x','x','y','x','y'},
                {'y','y','y','y','y'},
                {'x','x','x','y','y'},
        };
        int result = new Solution().numOfMatrix(arr);
        System.out.println(result);
    }

    int searchOne(char[][] arr, int x, int y) {
        arr[x][y] = 'z';

        // 如果有x就深入
        if (x - 1 >= 0 && arr[x - 1][y] == 'x') {
            return searchOne(arr, x - 1, y);
        }
        if (x + 1 < arr[1].length && arr[x + 1][y] == 'x') {
            return searchOne(arr, x + 1, y);
        }
        if (y - 1 >= 0 && arr[x][y - 1] == 'x') {
            return searchOne(arr, x, y - 1);
        }
        if (y + 1 < arr.length && arr[x][y + 1] == 'x') {
            return searchOne(arr, x, y + 1);
        }

        arr[x][y] = 'y';

        // 如果没有x,判断是否有遍历过的，有就回退
        if (x - 1 >= 0 && arr[x - 1][y] == 'z') {
            return searchOne(arr, x - 1, y);
        }
        if (x + 1 < arr[1].length && arr[x + 1][y] == 'z') {
            return searchOne(arr, x + 1, y);
        }
        if (y - 1 >= 0 && arr[x][y - 1] == 'z') {
            return searchOne(arr, x, y - 1);
        }
        if (y + 1 < arr.length && arr[x][y + 1] == 'z') {
            return searchOne(arr, x, y + 1);
        }

        // 如果没有x,也没有z,则表明四周围都是墙，此处是一片，返回1
        return 1;
    }

    int numOfMatrix(char[][] arr) {
        int m = arr.length;
        int n = arr[1].length;
        int result = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (arr[i][j] == 'x') { // 从起始点开始搜索 搜完一片区域后 得到一片区域
                    result += searchOne(arr, i,j);
                }
            }
        }

        return result;
    }
}
