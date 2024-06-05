//
//  HMInputOpData.h
//  HMCloudPlayerCore
//
//  Created by apple on 2023/12/12.
//  Copyright © 2023 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface HMCoordinatePos : NSObject

@property(nonatomic,assign) NSInteger x;
@property(nonatomic,assign) NSInteger y;

@end

///** 三种标准设备操作 */
typedef NS_ENUM(NSInteger,HMOneInputOPData_InputOP) {
  /** 键盘，按照虚拟键盘码定义 */
  HMOneInputOPData_InputOP_OpKeyBegin = 0,

  /** 鼠标左键 */
  HMOneInputOPData_InputOP_OpKeyVkLbutton = 1,

  /** 鼠标右键 */
  HMOneInputOPData_InputOP_OpKeyVkRbutton = 2,

  /** Cancel */
  HMOneInputOPData_InputOP_OpKeyVkCancel = 3,

  /** 鼠标中键 */
  HMOneInputOPData_InputOP_OpKeyVkMbutton = 4,

  /** */
  HMOneInputOPData_InputOP_OpKeyVkXbutton1 = 5,

  /** */
  HMOneInputOPData_InputOP_OpKeyVkXbutton2 = 6,

  /** Backspace */
  HMOneInputOPData_InputOP_OpKeyVkBack = 8,

  /** Tab */
  HMOneInputOPData_InputOP_OpKeyVkTab = 9,

  /** Clear */
  HMOneInputOPData_InputOP_OpKeyVkClear = 12,

  /** Enter */
  HMOneInputOPData_InputOP_OpKeyVkReturn = 13,

  /** Shift */
  HMOneInputOPData_InputOP_OpKeyVkShift = 16,

  /** Ctrl */
  HMOneInputOPData_InputOP_OpKeyVkControl = 17,

  /** Alt */
  HMOneInputOPData_InputOP_OpKeyVkMenu = 18,

  /** Pause */
  HMOneInputOPData_InputOP_OpKeyVkPause = 19,

  /** Caps Lock */
  HMOneInputOPData_InputOP_OpKeyVkCapital = 20,
  HMOneInputOPData_InputOP_OpKeyVkKana = 21,
  HMOneInputOPData_InputOP_OpKeyVkHangul = 21,
  HMOneInputOPData_InputOP_OpKeyVkJunja = 23,
  HMOneInputOPData_InputOP_OpKeyVkFinal = 24,
  HMOneInputOPData_InputOP_OpKeyVkHanja = 25,
  HMOneInputOPData_InputOP_OpKeyVkKanji = 25,

  /** Esc */
  HMOneInputOPData_InputOP_OpKeyVkEscape = 27,
  HMOneInputOPData_InputOP_OpKeyVkConvert = 28,
  HMOneInputOPData_InputOP_OpKeyVkNonconvert = 29,
  HMOneInputOPData_InputOP_OpKeyVkAccept = 30,
  HMOneInputOPData_InputOP_OpKeyVkModechange = 31,

  /** Space */
  HMOneInputOPData_InputOP_OpKeyVkSpace = 32,

  /** Page Up */
  HMOneInputOPData_InputOP_OpKeyVkPrior = 33,

  /** Page Down */
  HMOneInputOPData_InputOP_OpKeyVkNext = 34,

  /** End */
  HMOneInputOPData_InputOP_OpKeyVkEnd = 35,

  /** Home */
  HMOneInputOPData_InputOP_OpKeyVkHome = 36,

  /** Left Arrow */
  HMOneInputOPData_InputOP_OpKeyVkLeft = 37,

  /** Up Arrow */
  HMOneInputOPData_InputOP_OpKeyVkUp = 38,

  /** Right Arrow */
  HMOneInputOPData_InputOP_OpKeyVkRight = 39,

  /** Down Arrow */
  HMOneInputOPData_InputOP_OpKeyVkDown = 40,

  /** Select */
  HMOneInputOPData_InputOP_OpKeyVkSelect = 41,

  /** Print */
  HMOneInputOPData_InputOP_OpKeyVkPrint = 42,

  /** Execute */
  HMOneInputOPData_InputOP_OpKeyVkExecute = 43,

  /** Snapshot */
  HMOneInputOPData_InputOP_OpKeyVkSnapshot = 44,

  /** Insert */
  HMOneInputOPData_InputOP_OpKeyVkInsert = 45,

  /** Delete */
  HMOneInputOPData_InputOP_OpKeyVkDelete = 46,

  /** Help */
  HMOneInputOPData_InputOP_OpKeyVkHelp = 47,

  /** 0 */
  HMOneInputOPData_InputOP_OpKeyKey0 = 48,

  /** 1 */
  HMOneInputOPData_InputOP_OpKeyKey1 = 49,

  /** 2 */
  HMOneInputOPData_InputOP_OpKeyKey2 = 50,

  /** 3 */
  HMOneInputOPData_InputOP_OpKeyKey3 = 51,

  /** 4 */
  HMOneInputOPData_InputOP_OpKeyKey4 = 52,

  /** 5 */
  HMOneInputOPData_InputOP_OpKeyKey5 = 53,

  /** 6 */
  HMOneInputOPData_InputOP_OpKeyKey6 = 54,

  /** 7 */
  HMOneInputOPData_InputOP_OpKeyKey7 = 55,

  /** 8 */
  HMOneInputOPData_InputOP_OpKeyKey8 = 56,

  /** 9 */
  HMOneInputOPData_InputOP_OpKeyKey9 = 57,

  /** A */
  HMOneInputOPData_InputOP_OpKeyKeyA = 65,

  /** B */
  HMOneInputOPData_InputOP_OpKeyKeyB = 66,

  /** C */
  HMOneInputOPData_InputOP_OpKeyKeyC = 67,

  /** D */
  HMOneInputOPData_InputOP_OpKeyKeyD = 68,

  /** E */
  HMOneInputOPData_InputOP_OpKeyKeyE = 69,

  /** F */
  HMOneInputOPData_InputOP_OpKeyKeyF = 70,

  /** G */
  HMOneInputOPData_InputOP_OpKeyKeyG = 71,

  /** H */
  HMOneInputOPData_InputOP_OpKeyKeyH = 72,

  /** I */
  HMOneInputOPData_InputOP_OpKeyKeyI = 73,

  /** J */
  HMOneInputOPData_InputOP_OpKeyKeyJ = 74,

  /** K */
  HMOneInputOPData_InputOP_OpKeyKeyK = 75,

  /** L */
  HMOneInputOPData_InputOP_OpKeyKeyL = 76,

  /** M */
  HMOneInputOPData_InputOP_OpKeyKeyM = 77,

  /** N */
  HMOneInputOPData_InputOP_OpKeyKeyN = 78,

  /** O */
  HMOneInputOPData_InputOP_OpKeyKeyO = 79,

  /** P */
  HMOneInputOPData_InputOP_OpKeyKeyP = 80,

  /** Q */
  HMOneInputOPData_InputOP_OpKeyKeyQ = 81,

  /** R */
  HMOneInputOPData_InputOP_OpKeyKeyR = 82,

  /** S */
  HMOneInputOPData_InputOP_OpKeyKeyS = 83,

  /** T */
  HMOneInputOPData_InputOP_OpKeyKeyT = 84,

  /** U */
  HMOneInputOPData_InputOP_OpKeyKeyU = 85,

  /** V */
  HMOneInputOPData_InputOP_OpKeyKeyV = 86,

  /** W */
  HMOneInputOPData_InputOP_OpKeyKeyW = 87,

  /** X */
  HMOneInputOPData_InputOP_OpKeyKeyX = 88,

  /** Y */
  HMOneInputOPData_InputOP_OpKeyKeyY = 89,

  /** Z */
  HMOneInputOPData_InputOP_OpKeyKeyZ = 90,
  HMOneInputOPData_InputOP_OpKeyVkLwin = 91,
  HMOneInputOPData_InputOP_OpKeyVkRwin = 92,
  HMOneInputOPData_InputOP_OpKeyVkApps = 93,
  HMOneInputOPData_InputOP_OpKeyVkSleep = 95,

  /** 小键盘 0 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad0 = 96,

  /** 小键盘 1 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad1 = 97,

  /** 小键盘 2 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad2 = 98,

  /** 小键盘 3 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad3 = 99,

  /** 小键盘 4 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad4 = 100,

  /** 小键盘 5 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad5 = 101,

  /** 小键盘 6 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad6 = 102,

  /** 小键盘 7 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad7 = 103,

  /** 小键盘 8 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad8 = 104,

  /** 小键盘 9 */
  HMOneInputOPData_InputOP_OpKeyVkNumpad9 = 105,

  /** 小键盘 * */
  HMOneInputOPData_InputOP_OpKeyVkMultiply = 106,

  /** 小键盘 + */
  HMOneInputOPData_InputOP_OpKeyVkAdd = 107,

  /** 小键盘 Enter */
  HMOneInputOPData_InputOP_OpKeyVkSeparator = 108,

  /** 小键盘 - */
  HMOneInputOPData_InputOP_OpKeyVkSubtract = 109,

  /** 小键盘 . */
  HMOneInputOPData_InputOP_OpKeyVkDecimal = 110,

  /** 小键盘 / */
  HMOneInputOPData_InputOP_OpKeyVkDivide = 111,

  /** F1 */
  HMOneInputOPData_InputOP_OpKeyVkF1 = 112,

  /** F2 */
  HMOneInputOPData_InputOP_OpKeyVkF2 = 113,

  /** F3 */
  HMOneInputOPData_InputOP_OpKeyVkF3 = 114,

  /** F4 */
  HMOneInputOPData_InputOP_OpKeyVkF4 = 115,

  /** F5 */
  HMOneInputOPData_InputOP_OpKeyVkF5 = 116,

  /** F6 */
  HMOneInputOPData_InputOP_OpKeyVkF6 = 117,

  /** F7 */
  HMOneInputOPData_InputOP_OpKeyVkF7 = 118,

  /** F8 */
  HMOneInputOPData_InputOP_OpKeyVkF8 = 119,

  /** F9 */
  HMOneInputOPData_InputOP_OpKeyVkF9 = 120,

  /** F10 */
  HMOneInputOPData_InputOP_OpKeyVkF10 = 121,

  /** F11 */
  HMOneInputOPData_InputOP_OpKeyVkF11 = 122,

  /** F12 */
  HMOneInputOPData_InputOP_OpKeyVkF12 = 123,
  HMOneInputOPData_InputOP_OpKeyVkF13 = 124,
  HMOneInputOPData_InputOP_OpKeyVkF14 = 125,
  HMOneInputOPData_InputOP_OpKeyVkF15 = 126,
  HMOneInputOPData_InputOP_OpKeyVkF16 = 127,
  HMOneInputOPData_InputOP_OpKeyVkF17 = 128,
  HMOneInputOPData_InputOP_OpKeyVkF18 = 129,
  HMOneInputOPData_InputOP_OpKeyVkF19 = 130,
  HMOneInputOPData_InputOP_OpKeyVkF20 = 131,
  HMOneInputOPData_InputOP_OpKeyVkF21 = 132,
  HMOneInputOPData_InputOP_OpKeyVkF22 = 133,
  HMOneInputOPData_InputOP_OpKeyVkF23 = 134,
  HMOneInputOPData_InputOP_OpKeyVkF24 = 135,

  /** Num Lock */
  HMOneInputOPData_InputOP_OpKeyVkNumlock = 144,

  /** Scroll */
  HMOneInputOPData_InputOP_OpKeyVkScroll = 145,
  HMOneInputOPData_InputOP_OpKeyVkLshift = 160,
  HMOneInputOPData_InputOP_OpKeyVkRshift = 161,
  HMOneInputOPData_InputOP_OpKeyVkLcontrol = 162,
  HMOneInputOPData_InputOP_OpKeyVkRcontrol = 163,
  HMOneInputOPData_InputOP_OpKeyVkLmenu = 164,
  HMOneInputOPData_InputOP_OpKeyVkRmenu = 165,
  HMOneInputOPData_InputOP_OpKeyVkBrowserBack = 166,
  HMOneInputOPData_InputOP_OpKeyVkBrowserForward = 167,
  HMOneInputOPData_InputOP_OpKeyVkBrowserRefresh = 168,
  HMOneInputOPData_InputOP_OpKeyVkBrowserStop = 169,
  HMOneInputOPData_InputOP_OpKeyVkBrowserSearch = 170,
  HMOneInputOPData_InputOP_OpKeyVkBrowserFavorites = 171,
  HMOneInputOPData_InputOP_OpKeyVkBrowserHome = 172,

  /** VolumeMute */
  HMOneInputOPData_InputOP_OpKeyVkVolumeMut = 173,

  /** VolumeDown */
  HMOneInputOPData_InputOP_OpKeyVkVolumeDown = 174,

  /** Vol p */
  HMOneInputOPData_InputOP_OpKeyVkVolumeUp = 175,
  HMOneInputOPData_InputOP_OpKeyVkMediaNextTrack = 176,
  HMOneInputOPData_InputOP_OpKeyVkMediaPrevTrack = 177,
  HMOneInputOPData_InputOP_OpKeyVkMediaStop = 178,
  HMOneInputOPData_InputOP_OpKeyVkMediaPlayPause = 179,
  HMOneInputOPData_InputOP_OpKeyVkLaunchMail = 180,
  HMOneInputOPData_InputOP_OpKeyVkLaunchMediaSelect = 181,
  HMOneInputOPData_InputOP_OpKeyVkLaunchApp1 = 182,
  HMOneInputOPData_InputOP_OpKeyVkLaunchApp2 = 183,

  /** ; : */
  HMOneInputOPData_InputOP_OpKeyVkOem1 = 186,

  /** = + */
  HMOneInputOPData_InputOP_OpKeyVkOemPlus = 187,

  /** */
  HMOneInputOPData_InputOP_OpKeyVkOemComma = 188,

  /** - _ */
  HMOneInputOPData_InputOP_OpKeyVkOemMinus = 189,

  /** */
  HMOneInputOPData_InputOP_OpKeyVkOemPeriod = 190,

  /** / ? */
  HMOneInputOPData_InputOP_OpKeyVkOem2 = 191,

  /** ` ~ */
  HMOneInputOPData_InputOP_OpKeyVkOem3 = 192,

  /** [ { */
  HMOneInputOPData_InputOP_OpKeyVkOem4 = 219,

  /** \\ | */
  HMOneInputOPData_InputOP_OpKeyVkOem5 = 220,

  /** ] } */
  HMOneInputOPData_InputOP_OpKeyVkOem6 = 221,

  /** ' " */
  HMOneInputOPData_InputOP_OpKeyVkOem7 = 222,
  HMOneInputOPData_InputOP_OpKeyVkOem8 = 223,
  HMOneInputOPData_InputOP_OpKeyVkOem102 = 226,
  HMOneInputOPData_InputOP_OpKeyVkPacket = 231,
  HMOneInputOPData_InputOP_OpKeyVkProcesskey = 229,
  HMOneInputOPData_InputOP_OpKeyVkAttn = 246,
  HMOneInputOPData_InputOP_OpKeyVkCrsel = 247,
  HMOneInputOPData_InputOP_OpKeyVkExsel = 248,
  HMOneInputOPData_InputOP_OpKeyVkEreof = 249,
  HMOneInputOPData_InputOP_OpKeyVkPlay = 250,
  HMOneInputOPData_InputOP_OpKeyVkZoom = 251,
  HMOneInputOPData_InputOP_OpKeyVkNoname = 252,
  HMOneInputOPData_InputOP_OpKeyVkPa1 = 253,
  HMOneInputOPData_InputOP_OpKeyVkOemClear = 254,

  /** 键盘消息范围结束 */
  HMOneInputOPData_InputOP_OpKeyEnd = 510,

  /**
   * 下面操作装置为自定义
   * 鼠标
   **/
  HMOneInputOPData_InputOP_OpMouseBegin = 511,
  HMOneInputOPData_InputOP_OpMouseButtonLeft = 512,
  HMOneInputOPData_InputOP_OpMouseButtonMiddle = 513,
  HMOneInputOPData_InputOP_OpMouseButtonRight = 514,

  /** 鼠标滚轮, value>0向上滚, 小于0则向下滚 */
  HMOneInputOPData_InputOP_OpMouseWheel = 515,

  /** 鼠标移动 */
  HMOneInputOPData_InputOP_OpMouseMov = 516,

  /**
   * OP_MOUSE_CURSOR_MOV             = 516;  // 光标移动
   * OP_MOUSE_PHY_MOV                = 517;  // 实际鼠标移动
   **/
  HMOneInputOPData_InputOP_OpMouseEnd = 766,

  /**
   * 手柄
   * 标准键
   **/
  HMOneInputOPData_InputOP_OpGamepadBegin = 767,

  /** 右边按键：下   × */
  HMOneInputOPData_InputOP_OpGamepadBtnA = 768,

  /** 右边按键：右   ○ */
  HMOneInputOPData_InputOP_OpGamepadBtnB = 769,

  /** 右边按键：左   □ */
  HMOneInputOPData_InputOP_OpGamepadBtnC = 770,

  /** 右边按键：上   △ */
  HMOneInputOPData_InputOP_OpGamepadBtnD = 771,
  HMOneInputOPData_InputOP_OpGamepadBtnL1 = 772,
  HMOneInputOPData_InputOP_OpGamepadBtnR1 = 773,
  HMOneInputOPData_InputOP_OpGamepadBtnL2 = 774,
  HMOneInputOPData_InputOP_OpGamepadBtnR2 = 775,
  HMOneInputOPData_InputOP_OpGamepadBtnSelect = 776,
  HMOneInputOPData_InputOP_OpGamepadBtnStart = 777,

  /** 左轴按键，充当11键 */
  HMOneInputOPData_InputOP_OpGamepadBtnLa = 778,

  /** 右轴按键，充当12键 */
  HMOneInputOPData_InputOP_OpGamepadBtnRa = 779,
  HMOneInputOPData_InputOP_OpGamepadBtnMode = 780,
  HMOneInputOPData_InputOP_OpGamepadBtnTurbo = 781,
  HMOneInputOPData_InputOP_OpGamepadBtnClear = 782,

  /** 方向轴 */
  HMOneInputOPData_InputOP_OpGaempadAxisPov0 = 783,

  /** 轴 */
  HMOneInputOPData_InputOP_OpGamepadAxisX = 784,

  /** 左摇杆上下 */
  HMOneInputOPData_InputOP_OpGamepadAxisY = 785,

  /** 右摇杆左右 */
  HMOneInputOPData_InputOP_OpGamepadAxisZ = 786,

  /** 右摇杆上下 视角 */
  HMOneInputOPData_InputOP_OpGamepadAxisZRotation = 787,

  /** 手柄消息结束 */
  HMOneInputOPData_InputOP_OpGamepadEnd = 1022,

  /** XBOX手柄按键数据 */
  HMOneInputOPData_InputOP_OpXinputBegin = 1023,

  /** 所有按键, 14键 */
  HMOneInputOPData_InputOP_OpXinputButtons = 1024,

  /** L2 */
  HMOneInputOPData_InputOP_OpXinputLeftTrigger = 1025,

  /** R2 */
  HMOneInputOPData_InputOP_OpXinputRightTrigger = 1026,

  /** 左摇杆 */
  HMOneInputOPData_InputOP_OpXinputThumbLx = 1027,
  HMOneInputOPData_InputOP_OpXinputThumbLy = 1028,

  /** 右摇杆 */
  HMOneInputOPData_InputOP_OpXinputThumbRx = 1029,
  HMOneInputOPData_InputOP_OpXinputThumbRy = 1030,

  /** TODO：含义不明 */
  HMOneInputOPData_InputOP_OpXinputAButton = 1031,
  HMOneInputOPData_InputOP_OpXinputEnd = 1150,

  /** 触摸屏数据 */
  HMOneInputOPData_InputOP_OpTouchBegin = 1151,

  /** 滑动 */
  HMOneInputOPData_InputOP_OpTouchMove = 1152,

  /** 手指触控 */
  HMOneInputOPData_InputOP_OpTouchPoint = 1153,

  /** 动作取消 */
  HMOneInputOPData_InputOP_OpTouchCancel = 1154,
  HMOneInputOPData_InputOP_OpTouchEnd = 1250,
};

typedef NS_ENUM(NSInteger,HMOneInputOPData_InputState) {
  /** 通用 */
  HMOneInputOPData_InputState_OpStateDefault = 1,
  HMOneInputOPData_InputState_OpStateDown = 2,
  HMOneInputOPData_InputState_OpStateUp = 3,

  /** 键盘 */
  HMOneInputOPData_InputState_OpVkUntoggled = 4,
  HMOneInputOPData_InputState_OpVkToggled = 5,
};

@interface HMOneInputOPData : NSObject

@property(nonatomic, assign) HMOneInputOPData_InputOP inputOp;
@property(nonatomic, assign) HMOneInputOPData_InputState inputState;
@property(nonatomic, assign) NSInteger value;
///** 坐标，光标移动位置 */
@property(nonatomic, strong)  HMCoordinatePos *posCursor;
//
///** 坐标，鼠标物理位置 */
@property(nonatomic, strong)  HMCoordinatePos *posMouse;

@end

@interface HMInputOpData : NSObject

/** 操作list，服务器顺序处理 */
@property(nonatomic, strong) NSMutableArray<HMOneInputOPData*> *opListArray;

@end

NS_ASSUME_NONNULL_END
