#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检测Java项目中未使用的private方法、字段和局部变量的脚本
"""

import os
import re
import glob
from collections import defaultdict
import json

class UnusedMemberDetector:
    def __init__(self, root_dir):
        self.root_dir = root_dir
        self.java_files = []
        self.class_members = defaultdict(list)  # class_name -> [members]
        self.member_usage = defaultdict(set)  # member_name -> set of files that use it
        
    def collect_java_files(self):
        """收集所有Java文件"""
        for root, dirs, files in os.walk(self.root_dir):
            for file in files:
                if file.endswith('.java'):
                    self.java_files.append(os.path.join(root, file))
        print(f"找到 {len(self.java_files)} 个Java文件")
        
    def extract_class_info(self, file_path):
        """从Java文件中提取类信息和成员定义"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                
            # 找到类名
            class_match = re.search(r'class\s+(\w+)', content)
            if not class_match:
                return None
                
            class_name = class_match.group(1)
            relative_path = os.path.relpath(file_path, self.root_dir)
            
            # 提取private字段
            private_fields = re.findall(r'private\s+(?:static\s+)?(?:final\s+)?(?:[\w<>[\]]+\s+)?(\w+)', content)
            
            # 提取private方法
            private_methods = re.findall(r'private\s+(?:static\s+)?(?:[\w<>[\]]+\s+)?(\w+)\s*\([^)]*\)', content)
            
            # 提取局部变量（在方法内部）
            methods = re.findall(r'(?:public|private|protected)?\s*(?:static\s+)?[\w<>[\]]+\s+(\w+)\s*\([^)]*\)\s*{([^}]+(?:{[^}]*}[^}]*)*)}', content, re.DOTALL)
            local_vars = []
            
            for method_name, method_body in methods:
                # 查找方法内部的局部变量声明
                method_vars = re.findall(r'(?:int|String|Long|Integer|Boolean|Date|List|Map|Set|Object|double|float|long)\s+(\w+)\s*=', method_body)
                local_vars.extend([(var, method_name) for var in method_vars])
            
            return {
                'class_name': class_name,
                'file_path': relative_path,
                'private_fields': private_fields,
                'private_methods': private_methods,
                'local_variables': local_vars
            }
        except Exception as e:
            print(f"处理文件 {file_path} 时出错: {e}")
            return None
    
    def check_member_usage(self, member_name, content):
        """检查成员是否在代码中被使用"""
        # 排除字段声明和方法声明
        usage_patterns = [
            rf'\b{re.escape(member_name)}\b(?!\s*[=;])',  # 字段使用（排除声明）
            rf'\b{re.escape(member_name)}\s*\(',  # 方法调用
        ]
        
        for pattern in usage_patterns:
            if re.search(pattern, content):
                return True
        return False
    
    def analyze_usage(self):
        """分析所有成员的使用情况"""
        for file_info in self.class_members.values():
            file_path = os.path.join(self.root_dir, file_info['file_path'])
            
            if not os.path.exists(file_path):
                continue
                
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # 检查private字段使用情况
                for field in file_info['private_fields']:
                    if self.check_member_usage(field, content):
                        self.member_usage[f"{file_info['class_name']}.{field}"].add(file_path)
                
                # 检查private方法使用情况
                for method in file_info['private_methods']:
                    # 排除getter/setter方法
                    if method.startswith('get') or method.startswith('set') or method.startswith('is'):
                        continue
                    if self.check_member_usage(method, content):
                        self.member_usage[f"{file_info['class_name']}.{method}()"].add(file_path)
                        
            except Exception as e:
                print(f"分析文件 {file_path} 时出错: {e}")
    
    def run_analysis(self):
        """运行完整分析"""
        print("开始分析...")
        self.collect_java_files()
        
        # 收集所有类的信息
        for file_path in self.java_files:
            info = self.extract_class_info(file_path)
            if info:
                self.class_members[info['file_path']] = info
        
        print(f"分析了 {len(self.class_members)} 个类")
        
        # 分析使用情况
        self.analyze_usage()
        
        return self.generate_report()
    
    def generate_report(self):
        """生成分析报告"""
        unused_members = []
        all_members = 0
        used_members = 0
        
        for file_path, file_info in self.class_members.items():
            class_name = file_info['class_name']
            
            # 检查未使用的private字段
            for field in file_info['private_fields']:
                member_key = f"{class_name}.{field}"
                all_members += 1
                if member_key not in self.member_usage:
                    unused_members.append({
                        'type': 'field',
                        'class': class_name,
                        'file': file_info['file_path'],
                        'name': field,
                        'category': 'private_field'
                    })
                else:
                    used_members += 1
            
            # 检查未使用的private方法（排除getter/setter）
            for method in file_info['private_methods']:
                if method.startswith('get') or method.startswith('set') or method.startswith('is'):
                    continue
                    
                member_key = f"{class_name}.{method}()"
                all_members += 1
                if member_key not in self.member_usage:
                    unused_members.append({
                        'type': 'method',
                        'class': class_name,
                        'file': file_info['file_path'],
                        'name': method,
                        'category': 'private_method'
                    })
                else:
                    used_members += 1
        
        report = {
            'summary': {
                'total_files': len(self.java_files),
                'total_classes': len(self.class_members),
                'total_members_checked': all_members,
                'used_members': used_members,
                'unused_members': len(unused_members),
                'unused_percentage': round(len(unused_members) / max(all_members, 1) * 100, 2)
            },
            'unused_members': unused_members[:50]  # 只显示前50个未使用的成员
        }
        
        return report

def main():
    root_dir = "src/main/java"
    if not os.path.exists(root_dir):
        print(f"目录 {root_dir} 不存在")
        return
    
    detector = UnusedMemberDetector(root_dir)
    report = detector.run_analysis()
    
    # 输出报告
    print("\n=== 未使用的private成员分析报告 ===")
    print(f"总文件数: {report['summary']['total_files']}")
    print(f"总类数: {report['summary']['total_classes']}")
    print(f"检查的成员数: {report['summary']['total_members_checked']}")
    print(f"已使用的成员数: {report['summary']['used_members']}")
    print(f"未使用的成员数: {report['summary']['unused_members']}")
    print(f"未使用比例: {report['summary']['unused_percentage']}%")
    
    if report['unused_members']:
        print("\n=== 前20个未使用的private成员 ===")
        for i, member in enumerate(report['unused_members'][:20], 1):
            print(f"{i:2d}. [{member['category']}] {member['class']}.{member['name']} ({member['file']})")
    else:
        print("\n没有发现未使用的private成员")

if __name__ == "__main__":
    main()