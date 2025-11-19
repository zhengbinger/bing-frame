package com.bing.framework.cache;

/**
 * 测试用户实体类
 * 
 * 用于缓存集成测试的用户对象
 * 
 * @author zhengbing
 * @date 2024-01-XX
 */
public class TestUser {
    
    private String id;
    private String name;
    private int age;
    
    public TestUser() {
    }
    
    public TestUser(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    @Override
    public String toString() {
        return "TestUser{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUser testUser = (TestUser) o;
        return age == testUser.age &&
                (id != null ? id.equals(testUser.id) : testUser.id == null) &&
                (name != null ? name.equals(testUser.name) : testUser.name == null);
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + age;
        return result;
    }
}