import React, { useState, useRef, useEffect, useCallback } from 'react';

/**
 * 滑动条验证码组件
 * 
 * 组件功能：
 * - 支持鼠标拖拽和触摸操作
 * - 实时验证滑动位置
 * - 支持自定义尺寸和样式
 * - 支持图片模式（背景图和滑块图）
 * - 响应式设计，适配移动端
 * 
 * 主要用途：
 * - 用户登录验证
 * - 表单提交验证
 * - 防止自动化攻击
 * 
 * @author zhengbing
 * @date 2024-12-23
 */
interface SliderCaptchaProps {
    // 验证码数据
    captchaData: {
        targetPosition: number;        // 目标位置
        tolerance: number;            // 容错范围
        currentPosition?: number;      // 当前滑块位置
        backgroundWidth?: number;      // 背景宽度
        sliderWidth?: number;          // 滑块宽度
        sliderHeight?: number;         // 滑块高度
        backgroundImageUrl?: string;   // 背景图片URL
        sliderImageUrl?: string;       // 滑块图片URL
        enableImageMode?: boolean;     // 是否启用图片模式
    };
    // 回调函数
    onValidate: (result: {
        success: boolean;
        position: number;
        isCompleted: boolean;
    }) => void;
    // 可选样式类名
    className?: string;
    // 组件宽度
    width?: number;
    // 是否禁用
    disabled?: boolean;
}

const SliderCaptcha: React.FC<SliderCaptchaProps> = ({
    captchaData,
    onValidate,
    className = '',
    width,
    disabled = false
}) => {
    const [isDragging, setIsDragging] = useState(false);
    const [currentPosition, setCurrentPosition] = useState(captchaData.currentPosition || 0);
    const [validationState, setValidationState] = useState<'idle' | 'validating' | 'success' | 'failed'>('idle');
    const [touchStartX, setTouchStartX] = useState(0);
    
    const sliderRef = useRef<HTMLDivElement>(null);
    const thumbRef = useRef<HTMLDivElement>(null);
    const trackRef = useRef<HTMLDivElement>(null);
    
    // 计算参数
    const trackWidth = width || captchaData.backgroundWidth || 300;
    const sliderWidth = captchaData.sliderWidth || 60;
    const sliderHeight = captchaData.sliderHeight || 40;
    const maxPosition = trackWidth - sliderWidth;
    
    // 组件初始化时设置随机起始位置
    useEffect(() => {
        if (captchaData.currentPosition === undefined) {
            setCurrentPosition(0);
        }
    }, [captchaData.captchaKey]);
    
    // 重置状态
    const reset = useCallback(() => {
        setCurrentPosition(0);
        setValidationState('idle');
        setIsDragging(false);
        if (thumbRef.current) {
            thumbRef.current.style.left = '0px';
            if (trackRef.current) {
                trackRef.current.style.setProperty('--slider-progress', '0%');
            }
        }
    }, []);
    
    // 开始拖拽
    const handleStart = useCallback((clientX: number, touchX?: number) => {
        if (disabled || validationState === 'success' || validationState === 'validating') return;
        
        setIsDragging(true);
        const rect = thumbRef.current?.getBoundingClientRect();
        if (rect && touchX !== undefined) {
            setTouchStartX(touchX - rect.left);
        } else if (rect) {
            setTouchStartX(clientX - rect.left);
        }
    }, [disabled, validationState]);
    
    // 拖拽中
    const handleMove = useCallback((clientX: number, touchX?: number) => {
        if (!isDragging || validationState === 'success') return;
        
        const rect = sliderRef.current?.getBoundingClientRect();
        if (!rect) return;
        
        let newPosition = touchX !== undefined 
            ? touchX - touchStartX - rect.left
            : clientX - touchStartX - rect.left;
        
        // 边界检查
        if (newPosition < 0) newPosition = 0;
        if (newPosition > maxPosition) newPosition = maxPosition;
        
        setCurrentPosition(newPosition);
        
        // 更新视觉效果
        if (thumbRef.current) {
            thumbRef.current.style.left = `${newPosition}px`;
        }
        if (trackRef.current) {
            trackRef.current.style.setProperty('--slider-progress', `${(newPosition / maxPosition) * 100}%`);
        }
    }, [isDragging, validationState, touchStartX, maxPosition]);
    
    // 结束拖拽
    const handleEnd = useCallback(() => {
        if (!isDragging) return;
        
        setIsDragging(false);
        
        // 验证位置
        const userPosition = Math.round(currentPosition);
        const targetPosition = captchaData.targetPosition;
        const tolerance = captchaData.tolerance || 5;
        
        const isValid = Math.abs(userPosition - targetPosition) <= tolerance;
        
        if (isValid) {
            setValidationState('success');
            onValidate({
                success: true,
                position: userPosition,
                isCompleted: true
            });
        } else {
            setValidationState('failed');
            onValidate({
                success: false,
                position: userPosition,
                isCompleted: false
            });
            
            // 失败后延迟重置
            setTimeout(() => {
                reset();
            }, 1500);
        }
    }, [isDragging, currentPosition, captchaData, onValidate, reset]);
    
    // 鼠标事件处理
    const handleMouseDown = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        handleStart(e.clientX);
    }, [handleStart]);
    
    const handleMouseMove = useCallback((e: MouseEvent) => {
        handleMove(e.clientX);
    }, [handleMove]);
    
    const handleMouseUp = useCallback(() => {
        handleEnd();
    }, [handleEnd]);
    
    // 触摸事件处理
    const handleTouchStart = useCallback((e: React.TouchEvent) => {
        e.preventDefault();
        const touch = e.touches[0];
        handleStart(0, touch.clientX);
    }, [handleStart]);
    
    const handleTouchMove = useCallback((e: TouchEvent) => {
        e.preventDefault();
        const touch = e.touches[0];
        handleMove(0, touch.clientX);
    }, [handleMove]);
    
    const handleTouchEnd = useCallback(() => {
        handleEnd();
    }, [handleEnd]);
    
    // 绑定全局事件监听器
    useEffect(() => {
        if (isDragging) {
            document.addEventListener('mousemove', handleMouseMove);
            document.addEventListener('mouseup', handleMouseUp);
            document.addEventListener('touchmove', handleTouchMove, { passive: false });
            document.addEventListener('touchend', handleTouchEnd);
        }
        
        return () => {
            document.removeEventListener('mousemove', handleMouseMove);
            document.removeEventListener('mouseup', handleMouseUp);
            document.removeEventListener('touchmove', handleTouchMove);
            document.removeEventListener('touchend', handleTouchEnd);
        };
    }, [isDragging, handleMouseMove, handleMouseUp, handleTouchMove, handleTouchEnd]);
    
    // 生成状态文本
    const getStatusText = () => {
        switch (validationState) {
            case 'success':
                return '验证成功 ✓';
            case 'failed':
                return '验证失败，请重试';
            case 'validating':
                return '验证中...';
            default:
                return '请拖拽滑块完成验证';
        }
    };
    
    // 生成状态样式
    const getStatusStyles = () => {
        switch (validationState) {
            case 'success':
                return { color: '#fff', backgroundColor: '#4CAF50' };
            case 'failed':
                return { color: '#fff', backgroundColor: '#f44336' };
            default:
                return { color: '#666', backgroundColor: 'transparent' };
        }
    };
    
    return (
        <div 
            ref={sliderRef}
            className={`slider-captcha ${className}`}
            style={{
                width: trackWidth,
                position: 'relative',
                userSelect: 'none'
            }}
        >
            <style>{`
                .slider-captcha .slider-track {
                    position: relative;
                    background: ${captchaData.enableImageMode ? `url(${captchaData.backgroundImageUrl}) center/cover no-repeat, #e0e0e0` : '#e0e0e0'};
                    border-radius: ${sliderHeight / 2}px;
                    cursor: ${disabled ? 'not-allowed' : 'pointer'};
                    overflow: hidden;
                    box-shadow: inset 0 2px 4px rgba(0,0,0,0.1);
                    height: ${sliderHeight}px;
                }
                
                .slider-captcha .slider-thumb {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: ${sliderWidth}px;
                    height: ${sliderHeight}px;
                    background: ${captchaData.enableImageMode 
                        ? `url(${captchaData.sliderImageUrl}) center/cover no-repeat, linear-gradient(45deg, #4CAF50, #45a049)`
                        : 'linear-gradient(45deg, #4CAF50, #45a049)'
                    };
                    border-radius: ${sliderHeight / 2}px;
                    cursor: ${disabled ? 'not-allowed' : 'grab'};
                    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
                    transition: box-shadow 0.2s;
                    z-index: 2;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 12px;
                    font-weight: bold;
                }
                
                .slider-captcha .slider-thumb:hover {
                    box-shadow: 0 4px 8px rgba(0,0,0,0.3);
                }
                
                .slider-captcha .slider-thumb:active,
                .slider-captcha .slider-thumb.is-dragging {
                    cursor: grabbing;
                }
                
                .slider-captcha .slider-text {
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    z-index: 1;
                    pointer-events: none;
                    font-size: 12px;
                    font-weight: 500;
                    text-align: center;
                    transition: all 0.3s ease;
                    max-width: 80%;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }
                
                .slider-captcha .slider-progress {
                    position: absolute;
                    top: 0;
                    left: 0;
                    height: 100%;
                    background: rgba(76, 175, 80, 0.1);
                    border-radius: ${sliderHeight / 2}px 0 0 ${sliderHeight / 2}px;
                    transition: width 0.1s ease;
                    z-index: 1;
                }
                
                .slider-captcha.validation-success .slider-thumb {
                    background: linear-gradient(45deg, #4CAF50, #45a049);
                    color: white;
                }
                
                .slider-captcha.validation-failed .slider-thumb {
                    background: linear-gradient(45deg, #f44336, #da190b);
                    color: white;
                }
                
                @media (max-width: 600px) {
                    .slider-captcha {
                        width: 100% !important;
                    }
                    
                    .slider-captcha .slider-track {
                        width: 100% !important;
                    }
                }
            `}</style>
            
            <div
                ref={trackRef}
                className={`slider-track ${validationState === 'success' ? 'validation-success' : ''} ${validationState === 'failed' ? 'validation-failed' : ''}`}
                style={{
                    width: trackWidth,
                    height: sliderHeight,
                    opacity: disabled ? 0.6 : 1
                }}
            >
                {/* 进度条 */}
                <div 
                    className="slider-progress"
                    style={{
                        width: `${(currentPosition / maxPosition) * 100}%`
                    }}
                />
                
                {/* 滑块 */}
                <div
                    ref={thumbRef}
                    className={`slider-thumb ${isDragging ? 'is-dragging' : ''}`}
                    style={{
                        left: currentPosition,
                        width: sliderWidth,
                        height: sliderHeight,
                        opacity: disabled ? 0.6 : 1,
                        cursor: disabled ? 'not-allowed' : (isDragging ? 'grabbing' : 'grab')
                    }}
                    onMouseDown={handleMouseDown}
                    onTouchStart={handleTouchStart}
                >
                    {validationState === 'success' ? '✓' : '≡'}
                </div>
                
                {/* 状态文本 */}
                <div 
                    className="slider-text"
                    style={{
                        ...getStatusStyles(),
                        fontSize: sliderHeight <= 40 ? '11px' : '12px'
                    }}
                >
                    {getStatusText()}
                </div>
            </div>
            
            {/* 重置按钮（验证失败时显示） */}
            {validationState === 'failed' && (
                <button
                    type="button"
                    onClick={reset}
                    style={{
                        marginTop: '10px',
                        padding: '5px 15px',
                        background: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '12px'
                    }}
                >
                    重新开始
                </button>
            )}
        </div>
    );
};

export default SliderCaptcha;