# Hibernate---ASM---Library-Management-System
# BÀI TẬP TỔNG HỢP HIBERNATE – FRESHER JAVA
**Dự án**: Library Management (Quản lý Thư viện)  
**Hình thức**: Ứng dụng Console (CLI), không dùng Spring Boot  
**CSDL**: Chọn *một* trong **MySQL / PostgreSQL / SQL Server**  
**Build**: Maven  
**Thời lượng gợi ý**: 7 ngày  
**Đối tượng**: Fresher Java  

---

## 1. Mục tiêu học tập (Learning Outcomes)
Sau khi hoàn thành bài tập, học viên có thể:
1. Cấu hình và vận hành Hibernate ORM với Maven (SessionFactory, Session, Transaction).
2. Thiết kế **Layered Architecture** rõ ràng: `Main → Service (Interface/Impl) → Repository (DAO) → Entities`.
3. Mapping đúng quan hệ `One-to-Many`, `Many-to-One`, `Many-to-Many`, dùng `@Version` cho optimistic locking.
4. Thực hiện đầy đủ **CRUD**, **Search + Paging + Sorting** (HQL/Criteria/Native), gọi **Stored Procedure** ở mức Hibernate.
5. Áp dụng **Hibernate Validator (JSR‑380)** để đảm bảo business logic và dữ liệu đầu vào.
6. Bật và sử dụng **Second‑Level Cache** cho entity, **Query Cache** cơ bản.
7. Viết **JUnit 5** unit tests cho các rule quan trọng; **Mockito** (bonus) để mock repository trong service test.
8. (Bonus) Thực hiện **Batch Processing** hiệu quả và an toàn.
9. (Bonus) Tích hợp **Spring Core** (spring‑core/beans/context) cho DI/transaction (không Spring Boot).

> **Lưu ý**: Bài này **không yêu cầu** đưa kèm code cấu hình, và **không cần** kịch bản SQL trong tài liệu nộp. Triển khai trực tiếp trong mã nguồn dự án.

---

## 2. Bài toán & Phạm vi
### 2.1 Bối cảnh
Xây dựng ứng dụng **Quản lý Thư viện** với các đối tượng: **Book**, **Author**, **Member**, **Borrowing**. Ứng dụng chạy dưới dạng **CLI** cho phép quản trị viên thực hiện các thao tác quản lý và báo cáo theo yêu cầu bên dưới.

### 2.2 Chức năng bắt buộc (Functional Scope)
1. **Quản lý Sách (Book)**  
   - Thêm, sửa, xoá, xem chi tiết, liệt kê.  
   - Tìm kiếm theo tiêu chí: tên, thể loại, tình trạng sẵn sàng, tác giả.  
   - Báo cáo **Top N** sách được mượn nhiều nhất.
2. **Quản lý Độc giả (Member)**  
   - Thêm, sửa, xoá, xem; tìm theo tên, email, điện thoại.  
   - Kiểm tra điều kiện được mượn (đang mượn bao nhiêu, còn đủ hạn mức hay không).
3. **Mượn/Trả (Borrowing)**  
   - Mượn **nhiều sách** trong **một transaction**.  
   - Trả sách (một hoặc nhiều), cập nhật trạng thái.  
   - Gia hạn hạn trả (extend due date).
4. **Báo cáo & Truy vấn nâng cao**  
   - Danh sách mượn **quá hạn** (Native Query).  
   - **Stored Procedure**: liệt kê sách mượn quá hạn > X ngày (tham số đầu vào X).
5. **Tìm kiếm nâng cao + Phân trang + Sắp xếp** cho Book/Member/Borrowing.

---

## 3. Kiến trúc & Mô hình
### 3.1 Kiến trúc yêu cầu
- **Layered Architecture**:  
  - **Main (CLI)**: điều hướng menu, nhận input, in kết quả.  
  - **Service Layer**: chứa business logic, điều phối transaction.  
  - **Repository (DAO)**: thao tác dữ liệu với Hibernate (không tự commit/rollback).  
  - **Entities**: ánh xạ bảng/quan hệ, có trường audit cơ bản (createdAt/updatedAt).  
- **Transaction boundary**: tại **Service** cho mỗi thao tác nghiệp vụ.  
- **Logging**: sử dụng các mức `INFO/DEBUG/WARN/ERROR` và `correlationId` (MDC) cho mỗi request.

### 3.2 Mô hình dữ liệu khái quát (Conceptual)
- **Book**: id, title, category, available, createdDate, version; quan hệ *N‑N* với **Author**, *1‑N* với **Borrowing**.  
- **Author**: id, name, birthYear; quan hệ *N‑N* với **Book**.  
- **Member**: id, name, email, phone, version; quan hệ *1‑N* với **Borrowing**.  
- **Borrowing**: id, borrowDate, dueDate, returnDate, status (BORROWED/RETURNED), version; quan hệ *N‑1* tới **Member** và **Book**.

> Không yêu cầu đính kèm sơ đồ ER; tuy nhiên, nhóm nên tự phác để thống nhất.

---

## 4. Business Rules & Validation
### 4.1 Business Rules (bắt buộc)
- **R1**: **Không xoá Book** nếu vẫn có **Borrowing** ở trạng thái **BORROWED**.  
- **R2**: Mỗi **Member** được mượn **tối đa 5** sách **đang active** (BORROWED).  
- **R3**: **Không cho mượn** nếu `Book.available = false`.  
- **R4**: Khi mượn thành công → đặt `Book.available = false`. Khi trả → `Book.available = true`.  
- **R5**: `dueDate` phải **sau** `borrowDate`.  
- **R6**: `Member.email` **duy nhất** trong hệ thống; `phone` đúng định dạng.  
- **R7**: Sử dụng **Optimistic Locking** (`version`) để tránh mất cập nhật khi thao tác đồng thời.

### 4.2 Validation (Hibernate Validator – JSR‑380)
- **Field‑level**:  
  - `title`, `name`: bắt buộc (không rỗng).  
  - `category`, `borrowDate`, `dueDate`: bắt buộc.  
  - `email`: định dạng hợp lệ; **duy nhất** (validator tuỳ chỉnh).  
  - `phone`: khớp mẫu (quy ước theo yêu cầu nội bộ – ví dụ VN 10–11 số).  
- **Class‑level** (đầu vào mượn sách):  
  - Tổng **active borrowings** của `memberId` + số sách mượn mới **≤ 5**.  
  - Tất cả `bookIds` đều ở trạng thái **available** tại thời điểm kiểm tra.  
  - `dueDate` hợp lệ (sau `borrowDate`).

### 4.3 Thông điệp & Xử lý lỗi
- Ném **NotFound**, **Validation**, **BusinessRuleViolation**, **DuplicateResource** với message rõ ràng.  
- Ghi log **WARN** khi vi phạm rule; **ERROR** khi có exception không kiểm soát.

---

## 5. Service Layer – Yêu cầu chi tiết
> Mỗi phương thức Service là **một transaction**; rollback khi có **Runtime/Business** exception. Trình bày đầy đủ **mục đích**, **tham số**, **điều kiện trước/sau**, **ngoại lệ**, **logging**.

### 5.1 BookService
**Phạm vi:** Quản lý vòng đời Book, báo cáo phổ biến, truy vấn theo tác giả, cache.
- **create(request)**  
  - *Trước:* request hợp lệ.  
  - *Sau:* Book được tạo; log INFO id mới.  
- **getById(id)** → trả về chi tiết; 404 nếu không tồn tại.  
- **update(id, request)** → cập nhật trường hợp lệ; dùng optimistic locking.  
- **delete(id)**  
  - *Ràng buộc:* tuân thủ **R1**; nếu vi phạm ném lỗi nghiệp vụ.  
- **search(criteria, pageRequest)**  
  - *Tiêu chí:* tiêu đề chứa, thể loại, available, tác giả, khoảng ngày tạo.  
  - *Phân trang & sắp xếp:* theo nhiều trường; trả về tổng số bản ghi và tổng số trang.  
- **listByAuthor(authorId, limit)** → danh sách sách theo tác giả.  
- **topBorrowed(limit)** → trả về danh sách Book mượn nhiều nhất.  
- **changeAvailability(id, available)** → thay đổi trạng thái (đảm bảo đồng bộ với R3/R4).  
- **findOverdueByDays(days)** → gọi Stored Procedure, trả về danh sách sách quá hạn > days.  
- **getCached(id)** → minh hoạ **2nd‑level cache** (lặp lại gọi để chứng minh cache hit).  

**Batch (Bonus):**
- **bulkImport(list, batchSize)** → nhập liệu hàng loạt theo lô; flush/clear theo chunk; log thời gian/throughput.  
- **bulkUpdateAvailability(map, batchSize)** → cập nhật trạng thái hàng loạt; tối ưu nạp nhiều id.

### 5.2 MemberService
- **register(request)**  
  - *Ràng buộc:* email **duy nhất**; validate phone.  
- **getById(id)**; **update(id, request)**; **delete(id)** (không xoá khi còn active borrowing).  
- **findByEmail(email)** → Optional.  
- **search(criteria, pageRequest)** → lọc theo tên/email/phone + paging/sorting.  
- **isEligibleToBorrow(memberId)** → TRUE nếu active borrowings ≤ 4.  
- **countActiveBorrowings(memberId)** → số lượng đang BORROWED.

### 5.3 BorrowingService
- **borrowBooks(memberId, bookIds, dueDate)**  
  - *Trước:* hợp lệ theo **R2/R3/R5**.  
  - *Sau:* tạo nhiều Borrowing trong **một transaction**; cập nhật `Book.available=false`.  
- **returnBooks(borrowingIds, returnDate)**  
  - *Sau:* cập nhật status RETURNED, set `returnDate`, đặt `Book.available=true`.  
- **extendDueDate(borrowingId, newDueDate)**  
  - *Ràng buộc:* `newDueDate` > `currentDueDate`.  
- **findByMember(memberId, pageRequest)** → lịch sử theo Member.  
- **findActiveByBook(bookId)** → các lần mượn đang active cho Book.  
- **findOverdue(referenceDate)** → danh sách quá hạn (native).  
- **findOverdueByDaysSp(days)** → danh sách quá hạn theo Stored Procedure.

### 5.4 Giao ước Paging & Sorting
- **PageRequest**: page (0‑based), size (>0), danh sách sort (field + chiều).  
- **Page<T>**: content, totalElements, totalPages, page, size.  
- **Chuẩn sắp xếp**: cho phép nhiều trường; ví dụ `title ASC, createdDate DESC`.

### 5.5 Logging & Audit trong Service
- **INFO**: tạo/cập nhật/xoá/mượn/trả (id, actor hoặc correlationId, duration).  
- **DEBUG**: tham số đầu vào (ẩn dữ liệu nhạy cảm).  
- **WARN**: khi vi phạm business rules/validation.  
- **ERROR**: exception + stack trace.  
- **MDC correlationId** cho mỗi thao tác ở Main/CLI.

---

## 6. Repository Layer – Yêu cầu
- Chỉ chịu trách nhiệm **truy cập dữ liệu** (CRUD, truy vấn động, native).  
- Không quản lý transaction.  
- Cung cấp phương thức hỗ trợ Service: kiểm tra tồn tại active borrowing theo book/member; truy vấn topBorrowed; gọi Stored Procedure; tìm kiếm có điều kiện, phân trang và sắp xếp.

---

## 7. Caching – Yêu cầu cơ bản
1. Bật **Second‑Level Cache** cho **Book** (chế độ phù hợp), dùng để tối ưu truy vấn đọc lặp lại.  
2. Bật **Query Cache** cho một số truy vấn đọc thường xuyên, ví dụ: topBorrowed, listByAuthor.  
3. Trình bày ngắn gọn trong README: những truy vấn nào được cache, chính sách TTL, lý do lựa chọn.  
4. Không cache các truy vấn biến động cao nếu không cần thiết (ví dụ danh sách Borrowing đang active).

---

## 8. Stored Procedure – Yêu cầu
- Cung cấp một Stored Procedure có **tham số đầu vào là số ngày**; trả về danh sách sách mượn **quá hạn** hơn tham số đó.  
- Gọi Stored Procedure ở cấp Hibernate (mapping/call), ánh xạ kết quả sang DTO/view phù hợp.  
- Đảm bảo có mục hướng dẫn ngắn trong README về cách khởi tạo SP tương ứng với DB đã chọn.

> **Không đính kèm kịch bản SQL** trong tài liệu nộp. Chỉ cần mô tả và triển khai trong dự án.

---

## 9. CLI – Yêu cầu trải nghiệm
Menu gợi ý (có thể điều chỉnh, nhưng phải đầy đủ chức năng bắt buộc):
1) **Sách**: Tạo / Sửa / Xoá / Xem; Tìm kiếm (tiêu chí + paging + sorting); Top N mượn nhiều.  
2) **Độc giả**: Tạo / Sửa / Xoá / Xem; Tìm kiếm; Kiểm tra eligibility mượn.  
3) **Mượn & Trả**: Mượn nhiều sách; Trả; Gia hạn.  
4) **Báo cáo**: Quá hạn (native); Quá hạn > X ngày (Stored Procedure).  
5) **Thoát**.

Hiển thị rõ thông báo thành công/thất bại; in ngắn gọn nguyên nhân lỗi/vi phạm rule.

---

## 10. Testing – Yêu cầu
### 10.1 Unit Test (bắt buộc, JUnit 5)
- Kiểm thử **Business Rules** trọng yếu:  
  - Không cho mượn > 5 sách; không cho mượn khi sách không available.  
  - Không xoá Book đang được mượn.  
  - Không đăng ký Member khi email trùng.  
  - Trả sách phải đặt status RETURNED và available=true.  
- Kiểm thử **Search + Paging + Sorting**: đúng kích thước trang, tổng số phần tử, thứ tự sắp xếp.

### 10.2 Mockito (bonus)
- Mock Repository trong Service test; xác minh tương tác (save/find/exists…).

### 10.3 Integration Test (bonus)
- Có thể cấu hình profile test riêng hoặc dùng DB in‑memory tương thích.  
- Kiểm thử query native, mapping kết quả Stored Procedure.

---

## 11. Batch Processing (bonus)
- Cung cấp chức năng nhập/cập nhật dữ liệu theo **lô** với kích thước batch cấu hình được.  
- Đảm bảo kiểm soát bộ nhớ (flush & clear theo chu kỳ) và log throughput.  
- Chính sách lỗi: fail‑fast toàn bộ hoặc rollback theo **chunk**, nêu rõ trong README.

---

## 12. (Bonus) Spring Core (không Spring Boot)
- Dùng Spring Core/Beans/Context để DI các Service/Repository, cấu hình transaction manager, và áp dụng `@Transactional` ở Service.  
- Giữ nguyên các yêu cầu khác (caching, validation, testing).

---

## 13. Bàn giao & Hướng dẫn nộp bài
**Deliverables bắt buộc:**
- Mã nguồn dự án Maven đầy đủ.  
- **README.md** mô tả: cách chọn DB/profile, cách khởi tạo Stored Procedure (mô tả, không đính kèm SQL), các chức năng đã làm, cách chạy CLI, chính sách cache, phạm vi test.  
- **Kết quả test** (bản in `mvn test`), danh sách test cases chính.  
- **Log mẫu** cho một số luồng chính (tạo sách, mượn, trả, báo cáo).

**Định dạng nộp:**
- Link Git repository hoặc gói nén `.zip`.  
- Ghi rõ **DB đã chọn** trong README.

**Tiêu chí chấm**: theo Rubric ở mục 14.

**Thời hạn**: do giảng viên ấn định (điền ngày giờ nộp cụ thể).

**Làm nhóm**: tối đa 2 người (tuỳ lớp). Nếu làm nhóm, ghi rõ phân công.

**Chuẩn đạo đức học thuật**: Nghiêm cấm sao chép nguyên văn; phải tự viết và hiểu code của mình. Trích dẫn nguồn tham khảo (nếu có).

---

## 14. Rubric đánh giá (100 điểm + bonus tối đa 15)
| Nhóm tiêu chí | Mô tả | Điểm |
|---|---|---:|
| **Kiến trúc & Tổ chức mã** | Phân lớp đúng; Service/Repo/Entity/DTO/Validation tách bạch; transaction tại Service | 12 |
| **Entity Mapping** | Quan hệ đúng; khoá ngoại; optimistic locking `version`; enum hợp lệ | 10 |
| **CRUD đầy đủ** | Đủ Create/Read/Update/Delete cho Book/Member/Borrowing (tuân thủ R1) | 10 |
| **Search + Paging + Sorting** | Tìm kiếm theo tiêu chí; phân trang 0‑based; sort đa trường; tổng đếm chính xác | 10 |
| **Business Rules** | Áp dụng R1–R7 nhất quán; rollback đúng khi vi phạm | 12 |
| **Validation (JSR‑380)** | Field + custom validators; thông điệp lỗi rõ ràng, nhất quán | 8 |
| **Stored Procedure** | Gọi SP qua Hibernate; ánh xạ kết quả đúng; tham số linh hoạt | 8 |
| **Caching** | 2nd‑level cache cho Book; query cache hợp lý; giải thích trong README | 8 |
| **Logging** | Mức log hợp lý; có correlationId; log nghiệp vụ & lỗi rõ ràng | 6 |
| **Testing (JUnit 5)** | Test các rule quan trọng & truy vấn; báo cáo kết quả test | 10 |
| **CLI UX** | Menu đầy đủ; thông báo rõ ràng; thao tác mượt | 6 |
| **Tài liệu (README)** | Hướng dẫn build/run; mô tả SP; chính sách cache; phạm vi test | 4 |
| **Tổng** |  | **100** |

**Bonus (+15 tối đa):**
- Mockito mock repository trong Service test (+5)  
- Batch Processing (import/update theo lô, đo throughput) (+7)  
- Spring Core cho DI/transaction, không dùng Boot (+3)

**Trừ điểm:**  
- In lộ query raw/`show_sql` không kiểm soát (−2)  
- Không rollback transaction khi vi phạm rule (−5)  
- Không tách Service/Repo (−5)

---

## 15. Checklist tự đánh giá trước khi nộp
- [ ] CRUD đầy đủ cho Book/Member/Borrowing, tuân thủ R1–R7  
- [ ] Search + Paging + Sorting cho cả 3 nhóm chức năng  
- [ ] Validator JSR‑380 và custom hoạt động như mong đợi  
- [ ] Gọi Stored Procedure qua Hibernate, trả về đúng dữ liệu  
- [ ] Bật 2nd‑level cache cho Book; query cache được dùng hợp lý  
- [ ] JUnit 5: test các rule quan trọng; có báo cáo kết quả  
- [ ] Logging có correlationId; thông điệp lỗi rõ ràng  
- [ ] README rõ ràng, có hướng dẫn chạy và mô tả các điểm chính  
- [ ] (Bonus) Mockito; (Bonus) Batch; (Bonus) Spring Core

---

**Kết thúc đề bài.**  
*Chúc bạn hoàn thành tốt và học được nhiều điều từ bài tập này!*