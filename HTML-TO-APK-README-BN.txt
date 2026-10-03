TradeGuard HTML-Ready Android Project
====================================

এই ভার্সনে মূল Android কাজগুলো বাদ দেওয়া হয়নি। শুধু MainActivity-এর পুরোনো native UI-কে একটি HTML UI দিয়ে replace করা হয়েছে। HTML থেকে Android bridge-এর মাধ্যমে আগের native functions চালানো হয়।

HTML UI-তে রাখা হয়েছে:
- Enable Accessibility
- Start Screen Sensor
- TEST: Lock Quotex Now
- Reset Loss Streak / Unlock
- Loss limit (default 4)
- Lock duration (default 12 hours)
- Treat 0.00 as LOSS
- Result ROI L/T/R/B
- Trades / Profit / Loss / Streak
- Lock status + countdown

Native services রাখা হয়েছে:
- MediaProjection screen capture
- ML Kit OCR
- RESULT (P/L) detection
- dynamic profit/loss amount parsing
- consecutive loss counter
- Quotex foreground detection
- Accessibility overlay lock

গুরুত্বপূর্ণ:
Pure HTML-কে কোনো সাধারণ HTML-to-APK website দিয়ে wrap করলে AccessibilityService, MediaProjection, ML Kit OCR এবং Quotex overlay-এর native ক্ষমতা পাওয়া যাবে না। তাই এই HTML-Ready project-এ HTML UI + Android native bridge একসাথে রাখা হয়েছে।

Build করতে Android project হিসেবে import করতে হবে। GitHub Actions/Codemagic-এর মতো Android Gradle builder-এও ব্যবহার করা যাবে, তবে project-এর Gradle wrapper jar আলাদাভাবে থাকতে হবে যদি builder সেটি চায়।
