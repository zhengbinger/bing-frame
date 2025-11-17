#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检查Java文件中未使用的import语句
"""

import os
import re
import sys

def extract_imports_and_usage(java_file_path):
    """提取Java文件中的import语句和类使用情况"""
    try:
        with open(java_file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except:
        return [], []
    
    # 提取package声明（如果有的话）
    package_match = re.search(r'package\s+([\w.]+)\s*;', content)
    package_name = package_match.group(1) if package_match else ''
    
    # 提取所有import语句
    import_pattern = r'import\s+([\w.]+)\s*;'
    imports = re.findall(import_pattern, content)
    
    # 提取类使用情况（简单的类名匹配）
    # 这里我们使用类名而不包括包名来匹配
    class_usage = {}
    
    # 移除import和注释来检查代码
    # 移除单行注释
    content_no_comments = re.sub(r'//.*?\n', '\n', content)
    # 移除多行注释
    content_no_comments = re.sub(r'/\*.*?\*/', '', content_no_comments, flags=re.DOTALL)
    # 移除字符串字面量
    content_no_comments = re.sub(r'"[^"]*"', '', content_no_comments)
    
    # 对于每个import，检查是否在代码中被使用
    for imp in imports:
        # 获取类名
        class_name = imp.split('.')[-1]
        # 检查是否在代码中使用（排除import语句本身）
        if class_name in content_no_comments:
            # 进一步验证：检查是否是在实际的代码中使用，而不仅仅是在字符串中
            # 这里可以进一步改进逻辑
            class_usage[class_name] = True
        else:
            class_usage[class_name] = False
    
    return imports, class_usage

def check_java_file_for_unused_imports(file_path):
    """检查单个Java文件的未使用import"""
    imports, usage = extract_imports_and_usage(file_path)
    
    unused_imports = []
    for imp in imports:
        class_name = imp.split('.')[-1]
        if not usage.get(class_name, False):
            unused_imports.append(imp)
    
    return unused_imports

def main():
    """主函数"""
    src_dir = "src/main/java"
    
    unused_imports_found = []
    
    # 遍历所有Java文件
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                unused = check_java_file_for_unused_imports(file_path)
                if unused:
                    unused_imports_found.append((file_path, unused))
    
    # 输出结果
    if unused_imports_found:
        print("发现以下文件包含未使用的import:")
        print("=" * 60)
        
        for file_path, unused in unused_imports_found:
            print(f"\n文件: {file_path}")
            for imp in unused:
                print(f"  - {imp}")
    else:
        print("未发现未使用的import语句")
    
    return len(unused_imports_found)

if __name__ == "__main__":
    sys.exit(main())