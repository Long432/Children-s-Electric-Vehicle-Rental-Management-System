// Hướng thiết kế: màn hình đèn giao thông cho bãi xe. Trạng thái xe/vé là thứ người bán cần liếc thấy đầu tiên,
// nên màu trạng thái là màu mạnh nhất; mọi thứ còn lại giữ trung tính, nút to cho thao tác một tay ngoài trời.
export const color = {
  bg: '#EDF1F5', card: '#FFFFFF', ink: '#17212B', sub: '#5B6772', line: '#D5DCE3',
  action: '#2457D6', go: '#12915F', wait: '#D99A00', stop: '#CF3B3B', idle: '#8A96A3',
};
export const radius = { card: 18, btn: 14, chip: 999 };
export const font = { title: 26, h: 18, body: 16, small: 13 };

// Tên họ phông sau khi nạp. Android không áp độ đậm cho phông tùy chỉnh nên mỗi độ đậm là một tên riêng.
// Tiêu đề dùng Baloo 2, chữ mô tả và nút dùng Be Vietnam Pro.
export const fonts = {
  body: 'BeVietnamPro_400Regular',
  bodySemi: 'BeVietnamPro_600SemiBold',
  bodyBold: 'BeVietnamPro_700Bold',
  title: 'Baloo2_700Bold',
  titleBold: 'Baloo2_800ExtraBold',
};

// Baloo 2 có phần trên và dưới cao, nên dòng chữ lớn cần chiều cao dòng rộng để dấu chồng không bị cắt.
export const lineHeight = { title: 38, h: 26, big: 40, small: 19 };
