package com.zwb.block;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zwb.model.BlockDTO;
import com.zwb.model.BlockVO;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zwb.constant.constant.BASE_URL;
import static com.zwb.constant.constant.token;

public class BlockHandler {
    public static BlockDTO getBlockById(String id) {
        String url = BASE_URL + "blocks/" + id;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String bodyString = response.body().string();
                JSONObject jsonResponse = JSONUtil.parseObj(bodyString);
                JSONObject blockData = jsonResponse.getJSONObject("data");

                // 将 JSON 对象转换为 Block 对象
                BlockDTO block = JSONUtil.toBean(blockData.toString(), BlockDTO.class);
                return block;

            } else {
                System.out.println(response.body());
                System.out.println("请求失败，状态码：" + response.code());
            }
        } catch (IOException e) {
            System.out.println("网络错误：" + e.getMessage());
        }
        return null;
    }

    public static List<BlockDTO> getBlockChildrenById(String id, String startCursor, int pageSize) {
        List<BlockDTO> childrenList = new ArrayList<>();

        String url = BASE_URL + "blocks/" + id + "/children";
        if (startCursor != null) {
            url += "?start_cursor=" + startCursor + "&page_size=" + pageSize;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String bodyString = response.body().string();
                JSONObject jsonResponse = JSONUtil.parseObj(bodyString);
                JSONArray childrenData = jsonResponse.getJSONArray("data");

                for (Object obj : childrenData) {
                    JSONObject childData = (JSONObject) obj;
                    BlockDTO childBlock = JSONUtil.toBean(childData.toString(), BlockDTO.class);
                    childrenList.add(childBlock);
                }

                String nextCursor = jsonResponse.getStr("next_cursor");
                boolean hasMore = jsonResponse.getBool("has_more");

                if (hasMore && nextCursor != null) {
                    List<BlockDTO> nextPageChildren = getBlockChildrenById(id, nextCursor, pageSize);
                    childrenList.addAll(nextPageChildren);
                }

            } else {
                System.out.println("请求失败，状态码：" + response.code());
            }
        } catch (IOException e) {
            System.out.println("网络错误：" + e.getMessage());
        }

        return childrenList;
    }

    public static String createBlock(BlockVO blockVO) {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "blocks";

        // 将 Java 对象转换为 JSON 字符串
        String json = JSONUtil.toJsonStr(blockVO);

        // 输出发送的 JSON 数据以便于调试
        System.out.println("发送的JSON数据: " + json);

        // 创建请求体
        RequestBody requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                json);

        // 创建 HTTP 请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .post(requestBody)
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = null;
                if (response.body() != null) {
                    responseBody = response.body().string();
                }
                return responseBody;
            } else {
                return "请求失败，状态码：" + response.code();
            }
        } catch (IOException e) {
            return "网络错误：" + e.getMessage();
        }
    }


    public static void main(String[] args) {
        BlockDTO block = getBlockById("vib6yF1KQfYUqL3aoA84wk");
        System.out.println(block.getBlockAlignment());
        System.out.println(block.getChildren());
        System.out.println(block.getContent());
        List<BlockDTO> childBlocks = getBlockChildrenById("vib6yF1KQfYUqL3aoA84wk",null,100);
        System.out.println("---- Block List ----");
        for (BlockDTO child : childBlocks) {
            System.out.println("Block ID: " + child.getId());
            System.out.println("Block Content: " + child.getContent());
            System.out.println("--------------------");
        }
        //////
        BlockVO blockVO = new BlockVO();
        BlockVO.Block[] blocks = new BlockVO.Block[2];

        // 创建第一个块：文本类型
        BlockVO.Block block1 = new BlockVO.Block();
        block1.setType("text");
        block1.setContent("Hello");
        block1.setText_alignment("center");

//        // 创建第二个块：标题类型
//        BlockVO.Block block2 = new BlockVO.Block();
//        block2.setType("heading");
        // 设置标题内容
        blocks[0] = block1;
        blockVO.setParent_id("vib6yF1KQfYUqL3aoA84wk"); // 请设置实际的父块ID
        blockVO.setBlocks(blocks);

        // 调用 createBlock 方法发送请求
        String result = createBlock(blockVO);
        System.out.println("创建块的结果： " + result);


    }
}
