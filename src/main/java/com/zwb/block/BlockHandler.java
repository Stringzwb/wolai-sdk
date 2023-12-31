package com.zwb.block;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zwb.model.Block.Block;
import com.zwb.model.Block.Page;
import com.zwb.model.Block.Quote;
import com.zwb.model.Block.Text;
import com.zwb.model.BlockDTO;
import com.zwb.model.BlockVO;
import com.zwb.templete.csTemplete.BugTemplete;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.zwb.constant.constant.BASE_URL;
import static com.zwb.constant.constant.token;

@Slf4j
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
                System.out.println(bodyString);
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
                log.error("请求失败，状态码：" + response.code());
                log.error("错误信息：" + response.body());
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
                System.out.println(responseBody);
                return responseBody;
            } else {
                log.error(response.toString());
                return "请求失败，状态码：" + response.code();
            }
        } catch (IOException e) {
            return "网络错误：" + e.getMessage();
        }
    }



    public static Boolean batchGenerateBlocks(String blockType, List<String> nameList, String parent_id) {
        BlockVO blockVO = new BlockVO();
        blockVO.setParent_id(parent_id);
        List<Block> blockList = new ArrayList<>();

        for (String name : nameList) {
            Block block = null;
            switch (blockType) {
                case "page":
                    Page pageBlock = new Page();
                    pageBlock.setContent(name);
                    block = pageBlock;
                    break;
                case "quote":
                    Quote quoteBlock = new Quote();
                    quoteBlock.setContent(name);
                    block = quoteBlock;
                    break;
                case "text":
                    Text textBlock = new Text();
                    textBlock.setContent(name);
                    block = textBlock;
                    break;
                default:
                    System.out.println("Invalid block type: " + blockType);
                    return false;
            }
            if (block != null) {
                blockList.add(block);
            }
        }

        blockVO.setBlocks(blockList);
        createBlock(blockVO);
        return true;
    }




    public static void main(String[] args) {
//        获取块信息demo
//        BlockDTO block = getBlockById("vib6yF1KQfYUqL3aoA84wk");
//        System.out.println(block.getBlockAlignment());
//        System.out.println(block.getChildren());
//        System.out.println(block.getContent());
//        List<BlockDTO> childBlocks = getBlockChildrenById("vib6yF1KQfYUqL3aoA84wk",null,100);
//        System.out.println("---- Block List ----");
//        for (BlockDTO child : childBlocks) {
//            System.out.println("Block ID: " + child.getId());
//            System.out.println("Block Content: " + child.getContent());
//            System.out.println("--------------------");
//        }

//        生成模板demo
//        BugTemplete bugTemplete = new BugTemplete();
//        bugTemplete.createTemplete("vib6yF1KQfYUqL3aoA84wk");

        //批量创建demo
        List<String> javaTopics = Arrays.asList("Java 基础", "Spring Framework", "Hibernate", "springboot",
                "springcloud", "Maven", "JUnit", "Log4j", "Java 8 Features", "JVM",
                "网络基础知识", "git", "swigger", "RESTful api","redis",
                "Java 设计模式");

        // 调用批量生成块的方法，类型设置为"page"
        Boolean result = batchGenerateBlocks("page", javaTopics, "wo2srfwK3GHmxHhUEc4uWp");
        System.out.println(result);

    }
}
