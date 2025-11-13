const fs = require('fs');
const path = require('path');

// 读取Markdown文件
const markdownFile = path.join(__dirname, 'DATA_PERMISSION_MANAGEMENT_SYSTEM.md');
const markdownContent = fs.readFileSync(markdownFile, 'utf8');

// 可靠的Markdown转HTML函数
function markdownToHtml(markdown) {
    // 1. 首先提取代码块作为占位符
    const codeBlocks = [];
    let html = markdown.replace(/```([a-z]*?)\r?\n([\s\S]*?)```/g, (match, lang, code) => {
        const placeholder = `__CODE_BLOCK_${codeBlocks.length}__`;
        codeBlocks.push({ lang: lang || 'text', code });
        return placeholder;
    });
    
    // 2. 提取表格作为占位符
    const tables = [];
    // 先分割文本为行
    const htmlLines = html.split('\n');
    const resultLines = [];
    let tableContent = [];
    let inTable = false;
    
    // 逐行分析，识别完整表格
    for (let i = 0; i < htmlLines.length; i++) {
        const line = htmlLines[i].trim();
        
        // 检查是否是表格行
        const isTableLine = line.startsWith('|') && line.endsWith('|');
        // 检查是否是表格分隔行
        const isSeparatorLine = line.match(/^\|\s*[-:\s|]+\|$/);
        
        if (isTableLine || isSeparatorLine) {
            // 在表格内或遇到新表格行
            inTable = true;
            tableContent.push(htmlLines[i]);
        } else if (inTable && line === '') {
            // 表格内的空行也包含在表格中
            tableContent.push(htmlLines[i]);
        } else if (inTable) {
            // 表格结束
            if (tableContent.length > 0) {
                // 添加表格占位符
                const placeholder = `__TABLE_${tables.length}__`;
                tables.push(tableContent.join('\n'));
                resultLines.push(placeholder);
                tableContent = [];
                inTable = false;
            }
            // 添加非表格行
            resultLines.push(htmlLines[i]);
        } else {
            // 非表格行
            resultLines.push(htmlLines[i]);
        }
    }
    
    // 处理最后一个可能的表格
    if (inTable && tableContent.length > 0) {
        const placeholder = `__TABLE_${tables.length}__`;
        tables.push(tableContent.join('\n'));
        resultLines.push(placeholder);
    }
    
    // 重建文本
    html = resultLines.join('\n');
    
    // 3. 处理标题
    html = html.replace(/^#{1}\s+(.*?)$/gm, '<h1>$1</h1>');
    html = html.replace(/^#{2}\s+(.*?)$/gm, '<h2>$1</h2>');
    html = html.replace(/^#{3}\s+(.*?)$/gm, '<h3>$1</h3>');
    html = html.replace(/^#{4}\s+(.*?)$/gm, '<h4>$1</h4>');
    html = html.replace(/^#{5}\s+(.*?)$/gm, '<h5>$1</h5>');
    html = html.replace(/^#{6}\s+(.*?)$/gm, '<h6>$1</h6>');
    
    // 4. 处理无序列表 - 使用更健壮的方法
    const lines = html.split('\n');
    let result = '';
    let inList = false;
    let listType = '';
    let listItems = [];
    
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const trimmedLine = line.trim();
        
        // 检查是否是列表项
        const isUnorderedList = trimmedLine.match(/^[*+-]\s+(.*)$/);
        const isOrderedList = trimmedLine.match(/^(\d+)\.\s+(.*)$/);
        
        if (isUnorderedList || isOrderedList) {
            const content = isUnorderedList ? isUnorderedList[1] : isOrderedList[2];
            const currentType = isUnorderedList ? 'ul' : 'ol';
            
            if (!inList) {
                // 开始新列表
                inList = true;
                listType = currentType;
                listItems = [`<li>${content}</li>`];
            } else if (listType !== currentType) {
                // 不同类型的列表，先闭合之前的
                result += `<${listType}>\n${listItems.join('\n')}\n</${listType}>\n`;
                listType = currentType;
                listItems = [`<li>${content}</li>`];
            } else {
                // 相同类型的列表，添加新项
                listItems.push(`<li>${content}</li>`);
            }
        } else {
            // 不是列表项，闭合当前列表
            if (inList) {
                result += `<${listType}>\n${listItems.join('\n')}\n</${listType}>\n`;
                inList = false;
                listType = '';
                listItems = [];
            }
            result += line + '\n';
        }
    }
    
    // 闭合最后一个列表
    if (inList) {
        result += `<${listType}>\n${listItems.join('\n')}\n</${listType}>\n`;
    }
    
    html = result;
    
    // 5. 处理粗体和斜体
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    
    // 6. 处理图片
    html = html.replace(/!\[([^\]]+)\]\(([^)]+)\)/g, '<img src="$2" alt="$1">');
    
    // 7. 处理链接
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
    
    // 8. 恢复代码块
    html = html.replace(/__CODE_BLOCK_(\d+)__/g, (match, index) => {
        const { lang, code } = codeBlocks[index];
        return `<pre><code class="language-${lang}">${code}</code></pre>`;
    });
    
    // 9. 恢复表格（转换为HTML表格）
    html = html.replace(/__TABLE_(\d+)__/g, (match, index) => {
        return convertTableToHtml(tables[index]);
    });
    
    // 10. 处理段落
    return processParagraphs(html);
}

// 转换Markdown表格为HTML
function convertTableToHtml(markdownTable) {
    const lines = markdownTable.trim().split('\n');
    let tableHtml = '<table>\n';
    let hasHeader = false;
    
    for (let i = 0; i < lines.length; i++) {
        let line = lines[i].trim();
        
        // 跳过空行
        if (line === '') continue;
        
        // 检查是否是分隔行
        if (line.match(/^\|\s*[-:\s|]+\|$/)) {
            hasHeader = true;
            continue;
        }
        
        // 确保行以|开始和结束
        if (!line.startsWith('|') || !line.endsWith('|')) {
            console.warn('表格行格式不正确:', line);
            continue;
        }
        
        // 分割单元格
        const cells = line.split('|')
            .slice(1, -1) // 移除开始和结束的空元素
            .map(cell => cell.trim());
        
        tableHtml += '  <tr>\n';
        
        // 第一个数据行（在分隔行之前）是表头
        const isHeaderRow = !hasHeader && i === 0;
        
        cells.forEach(cell => {
            const tag = isHeaderRow ? 'th' : 'td';
            tableHtml += `    <${tag}>${cell}</${tag}>\n`;
        });
        
        tableHtml += '  </tr>\n';
    }
    
    tableHtml += '</table>';
    return tableHtml;
}

// 处理段落，避免在块元素内添加p标签
function processParagraphs(html) {
    const lines = html.split('\n');
    const processedLines = [];
    let inBlockElement = false;
    let currentParagraph = '';
    
    for (const line of lines) {
        const trimmedLine = line.trim();
        
        // 检查块元素开始
        if (trimmedLine.match(/^<(h[1-6]|ul|ol|table|pre|blockquote)/)) {
            inBlockElement = true;
            // 先处理之前的段落
            if (currentParagraph.trim()) {
                processedLines.push(`<p>${currentParagraph.trim()}</p>`);
                currentParagraph = '';
            }
            processedLines.push(line);
        }
        // 检查块元素结束
        else if (trimmedLine.match(/^<\/(h[1-6]|ul|ol|table|pre|blockquote)/)) {
            processedLines.push(line);
            inBlockElement = false;
        }
        // 在块元素内部
        else if (inBlockElement) {
            processedLines.push(line);
        }
        // 空行处理
        else if (trimmedLine === '') {
            if (currentParagraph.trim()) {
                processedLines.push(`<p>${currentParagraph.trim()}</p>`);
                currentParagraph = '';
            }
            processedLines.push('');
        }
        // 普通文本行
        else {
            currentParagraph += (currentParagraph ? ' ' : '') + trimmedLine;
        }
    }
    
    // 处理最后一个段落
    if (currentParagraph.trim()) {
        processedLines.push(`<p>${currentParagraph.trim()}</p>`);
    }
    
    return processedLines.join('\n');
}

// 生成完整的HTML文档
function generateHtmlDocument(bodyContent) {
    const htmlTemplate = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>数据权限管理体系设计文档</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', 'Segoe UI', Roboto, Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 1000px;
            margin: 0 auto;
            padding: 20px;
            background-color: #fff;
        }
        h1 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 15px;
            border-bottom: 2px solid #3498db;
        }
        h2 {
            color: #2980b9;
            margin-top: 40px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eaeaea;
        }
        h3 {
            color: #16a085;
            margin-top: 30px;
        }
        h4 {
            color: #27ae60;
            margin-top: 25px;
        }
        h5, h6 {
            color: #f39c12;
        }
        p {
            margin: 15px 0;
            text-align: justify;
        }
        ul, ol {
            margin: 15px 0;
            padding-left: 30px;
        }
        li {
            margin-bottom: 8px;
        }
        pre {
            background-color: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 4px;
            padding: 15px;
            overflow-x: auto;
            font-family: Monaco, Consolas, 'Courier New', monospace;
            margin: 20px 0;
        }
        code {
            font-family: Monaco, Consolas, 'Courier New', monospace;
            background-color: #f8f9fa;
            padding: 2px 4px;
            border-radius: 3px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }
        tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        img {
            max-width: 100%;
            height: auto;
            margin: 20px auto;
            display: block;
        }
        a {
            color: #3498db;
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    ${bodyContent}
</body>
</html>`;
    
    return htmlTemplate;
}

// 执行转换
const htmlContent = markdownToHtml(markdownContent);
const fullHtml = generateHtmlDocument(htmlContent);

// 写入HTML文件
const outputFile = path.join(__dirname, 'DATA_PERMISSION_MANAGEMENT_SYSTEM.html');
fs.writeFileSync(outputFile, fullHtml, 'utf8');

console.log('HTML文件已生成: DATA_PERMISSION_MANAGEMENT_SYSTEM.html');
console.log('您可以通过浏览器打开该文件，然后使用浏览器的打印功能导出为PDF。');