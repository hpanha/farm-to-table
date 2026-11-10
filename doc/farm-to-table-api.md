# Farm-to-Table API (សម្រាប់ Frontend)

**Base URL:** `http://localhost:8083` (or `http://192.168.100.63:8083`)
**Header:** `Content-Type: application/json`
**Auth:** `Authorization: Bearer <token>` គ្រប់ endpoint លើកលែង `/api/auth/login` និង `/api/auth/register`

## ទូទៅ

- **Pagination (endpoint ដែលមាន `pageable`):** `?page=0&size=10&sort=id,desc` (page ចាប់ពី 0)។
  Response ជា Spring Page: `content[]`, `totalElements`, `totalPages`, `number`, `size`, `first`, `last`, `numberOfElements`, `empty`
- **Endpoint ដែលប្រើ `page`/`size` ផ្ទាល់ (default 0/10) ហើយ return array:** order-status-histories, order-details, delivery-fees, deliveries
- **ថ្ងៃ:** `yyyy-MM-dd` | **ម៉ោង:** `2026-11-01T09:53:28`
- **Status code:** 200/201 ជោគជ័យ, 204 លុបបាន, 400 validation, 401 គ្មាន/ខុស token, 404 រកមិនឃើញ, 409 ផ្ទុយច្បាប់ business
- OpenAPI spec បង្ហាញ 200 គ្រប់ទីកន្លែង ព្រោះគ្មាន annotation ។ ពិតប្រាកដ POST របស់ Settlement, ProductImage, StockTransaction ត្រឡប់ **201** ហើយ DELETE ត្រឡប់ **204** ។ Controller ផ្សេងអាចខុស សូមពិនិត្យក្នុង Postman ។

---

## 1. Auth `/api/auth`

| Method | Path | Body | Response |
|---|---|---|---|
| POST | `/api/auth/login` | `{ username*, password* }` | `{ token, username, roleId }` |
| POST | `/api/auth/register` | `{ username*, fullName, email, password, companyName, phoneNumber, roleId }` | string |

## 2. Users `/api/users`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/users` | pageable | Page<UserResponse> |
| POST | `/api/users` | CreateUserRequest | UserResponse |
| GET | `/api/users/{id}` | | UserResponse |
| PUT | `/api/users/{id}` | UpdateUserRequest | UserResponse |
| DELETE | `/api/users/{id}` | | |

**CreateUserRequest:** `username*` (4-50, `[a-zA-Z0-9._-]`), `fullName*`, `email`, `image`, `rolesId*`, `detail*` (UserDetailRequest), `company` (UserCompanyDto), `account*` (UserAccountRequest)
**UpdateUserRequest:** `fullName`, `image`, `status`, `rolesId`, `detail`, `company`, `account`
**UserDetailRequest:** `userId`, `nationality*`, `cardId*`, `address*`, `dateOfBirth*` (date), `placeOfBirth`, `maritalStatus`, `userDetailId`
**UserCompanyDto:** `companyName*`, `address`, `operationYears`, `map`, `partner`, `phoneNumber`, `type`
**UserAccountRequest:** `accountName*`, `accountType*`, `accountNumber*`, `bankName*`
**UserResponse:** `id`, `username`, `fullName`, `email`, `status`, `image`, `role` {id, name, permissions[{id, permissionName, module, canInsert, canRead, canUpdate, canDelete}]}, `detail` {id, userId, nationality, cardId, address, dateOfBirth, placeOfBirth, maritalStatus}, `company`, `accounts[]` {id, accountName, accountType, accountNumber, bankName, isVerified, isDefault}, `createdAt`, `tempPassword`

## 3. User Detail `/api/user_detail`

| Method | Path | Body | Response |
|---|---|---|---|
| POST | `/api/user_detail` | UserDetailRequest | UserDetailEntity |
| PUT | `/api/user_detail/{id}` | UserDetailRequest | UserDetailEntity |
| DELETE | `/api/user_detail/{id}` | | string |
| GET | `/api/user_detail/user/{userId}` | | UserDetailEntity |

## 4. Terms & Conditions `/api/terms-conditions`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/terms-conditions` | `type`, `status`, `keyword`, pageable | Page |
| POST | `/api/terms-conditions` | `{ type*, version*, content* }` | TermsConditionResponseDto |
| GET | `/api/terms-conditions/{id}` | | TermsConditionResponseDto |
| PUT | `/api/terms-conditions/{id}` | `{ version*, content* }` | TermsConditionResponseDto |
| DELETE | `/api/terms-conditions/{id}` | | |
| PATCH | `/api/terms-conditions/{id}/status` | `{ status* }` | TermsConditionResponseDto |
| GET | `/api/terms-conditions/active/{type}` | | TermsConditionResponseDto |

`type`: `[A-Za-z0-9_]` ≤50 | `version`: `[A-Za-z0-9._-]` ≤20
**Response:** `id`, `type`, `version`, `content`, `status`

## 5. User Term Agreements `/api/user-term-agreements`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/user-term-agreements` | `userId`, `termId`, `status`, pageable | Page |
| POST | `/api/user-term-agreements` | `{ userId*, termId* }` | UserTermAgreementResponseDto |
| GET | `/api/user-term-agreements/{id}` | | UserTermAgreementResponseDto |
| DELETE | `/api/user-term-agreements/{id}` | | |
| PATCH | `/api/user-term-agreements/{id}/status` | `{ status* }` | UserTermAgreementResponseDto |
| GET | `/api/user-term-agreements/check` | `userId*`, `type*` | `{ userId, type, termId, version, agreed }` |

**Response:** `id`, `userId`, `termId`, `termType`, `termVersion`, `agreeAt`, `ipAddress`, `userAgent`, `status`

## 6. Statuses `/api/statuses`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/statuses` | `keyword`, `type`, pageable | Page |
| POST | `/api/statuses` | `{ code*, type*, description }` | StatusResponseDto |
| GET | `/api/statuses/{id}` | | StatusResponseDto |
| PUT | `/api/statuses/{id}` | `{ description }` | StatusResponseDto |
| DELETE | `/api/statuses/{id}` | | |
| GET | `/api/statuses/type/{type}` | | StatusResponseDto[] |

**Response:** `id`, `code`, `description`, `type` | `code`/`type`: `[A-Za-z0-9_]` ≤50

## 7. Categories `/api/categories`

| Method | Path | Body | Response |
|---|---|---|---|
| GET | `/api/categories` | | CategoryResponse[] |
| POST | `/api/categories` | `{ name, description, status }` | CategoryResponse |
| GET | `/api/categories/{id}` | | CategoryResponse |
| PUT | `/api/categories/{id}` | CategoryRequest | CategoryResponse |
| DELETE | `/api/categories/{id}` | | |

**Response:** `id`, `name`, `description`, `status`

## 8. Products `/api/products`

| Method | Path | Body | Response |
|---|---|---|---|
| GET | `/api/products` | | ProductResponse[] |
| POST | `/api/products` | ProductRequest | ProductResponse |
| GET | `/api/products/{id}` | | ProductResponse |
| PUT | `/api/products/{id}` | ProductRequest | ProductResponse |
| DELETE | `/api/products/{id}` | | |

**ProductRequest:** `userId`, `categoryId`, `name`, `qty`, `unitPrice`, `farmerPrice`, `expectedHavestDate`, `expiredDate`, `location`
**ProductResponse:** `id` + field ដូចខាងលើ

## 9. Product Images `/api/products/{productId}/images`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/products/{productId}/images` | `path` (ស្វែងរក), pageable | Page<ProductImageResponseDto> |
| POST | `/api/products/{productId}/images` | `{ path* }` ≤500 | 201 ProductImageResponseDto |
| GET | `/api/products/{productId}/images/{id}` | | ProductImageResponseDto |
| PUT | `/api/products/{productId}/images/{id}` | `{ path* }` | ProductImageResponseDto |
| DELETE | `/api/products/{productId}/images/{id}` | | 204 |

**Response:** `{ id, productId, path }` | `productId + path` ស្ទួន → 409 | product មិនមាន → 404

## 10. Product Rejects `/api/product-rejects`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/product-rejects` | `userId`, `categoryId`, `status` (boolean), `keyword`, pageable | Page<ProductRejectDto> |
| POST | `/api/product-rejects` | ProductRejectDto | ProductRejectDto |
| GET | `/api/product-rejects/{id}` | | ProductRejectDto |
| PUT | `/api/product-rejects/{id}` | ProductRejectDto | ProductRejectDto |
| DELETE | `/api/product-rejects/{id}` | | |

**ProductRejectDto:** `id`, `userId*`, `categoryId*`, `name*`, `qty*`, `unitPrice`, `farmerPrice`, `expectedHavestDate`, `expiredDate`, `location`, `reason`, `availableQty`, `soldQty` (string), `status` (boolean)

## 11. Product Checkers `/api/product-checkers`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/product-checkers` | pageable | Page<ProductCheckerDto> |
| POST | `/api/product-checkers` | ProductCheckerDto | ProductCheckerDto |
| GET | `/api/product-checkers/{id}` | | ProductCheckerDto |
| PUT | `/api/product-checkers/{id}` | ProductCheckerDto | ProductCheckerDto |
| DELETE | `/api/product-checkers/{id}` | | |
| GET | `/api/product-checkers/product/{productId}` | | ProductCheckerDto[] |
| DELETE | `/api/product-checkers/product/{productId}` | | |

**ProductCheckerDto:** `id`, `productId*`, `checkBy*`, `checkedAt` (string)

## 12. Product Handovers `/api/product-handovers`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/product-handovers` | `fromType`, `fromId`, `toType`, `toId`, `status`, pageable | Page |
| POST | `/api/product-handovers` | `{ fromType*, fromId*, toType*, toId*, qty*, note ≤255 }` | ProductHandoverResponseDto |
| GET | `/api/product-handovers/{id}` | | ProductHandoverResponseDto |
| PUT | `/api/product-handovers/{id}` | ProductHandoverRequestDto | ProductHandoverResponseDto |
| DELETE | `/api/product-handovers/{id}` | | |
| PATCH | `/api/product-handovers/{id}/status` | `{ status*, receiveBy }` | ProductHandoverResponseDto |

**Response:** `id`, `fromType`, `fromId`, `toType`, `toId`, `qty`, `statusId`, `status`, `receiveBy`, `note`

## 13. Warehouses `/api/warehouses`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/warehouses` | `keyword`, pageable | Page<WareHouseResponseDto> |
| GET | `/api/warehouses/all` | | WareHouseResponseDto[] |
| POST | `/api/warehouses` | WareHouseRequestDto | WareHouseResponseDto |
| GET | `/api/warehouses/{id}` | | WareHouseResponseDto |
| PUT | `/api/warehouses/{id}` | WareHouseRequestDto | WareHouseResponseDto |
| DELETE | `/api/warehouses/{id}` | | |

**WareHouseRequestDto:** `name*` ≤100, `description` ≤255, `address*` ≤255, `capacity*`, `capacityUnit`
**Response:** `id` + field ដូចខាងលើ

## 14. Warehouse Stocks `/api/warehouse-stocks`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/warehouse-stocks` | `warehouseId`, `productId`, `lowStock`, pageable | Page |
| POST | `/api/warehouse-stocks` | `{ warehouseId*, productId*, qty* }` | WarehouseStockResponseDto |
| GET | `/api/warehouse-stocks/{id}` | | WarehouseStockResponseDto |
| PUT | `/api/warehouse-stocks/{id}` | `{ qty* }` | WarehouseStockResponseDto |
| DELETE | `/api/warehouse-stocks/{id}` | | |
| PATCH | `/api/warehouse-stocks/{id}/reserve` | `{ qty* }` | WarehouseStockResponseDto |
| PATCH | `/api/warehouse-stocks/{id}/release` | `{ qty* }` | WarehouseStockResponseDto |
| PATCH | `/api/warehouse-stocks/{id}/adjust` | `{ delta* }` | WarehouseStockResponseDto |

**Response:** `id`, `warehouseId` (string), `productId` (string), `qty`, `reservedQty`, `availableQty`

> ស្តុកគួរប្តូរតាម Stock Transactions (ផ្នែក 15) ដើម្បីឱ្យមានប្រវត្តិ ។ PUT/adjust/reserve ផ្ទាល់មិនកត់ប្រវត្តិទេ ។

## 15. Stock Transactions `/api/stock-transactions`

គ្មាន PUT/DELETE (ledger កែមិនបាន) ។

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/stock-transactions` | `warehouseId`, `productId`, `type`, `statusId`, `from`, `to`, pageable | Page (id ថយក្រោយ) |
| POST | `/api/stock-transactions` | StockTransactionRequestDto | 201 StockTransactionResponseDto |
| GET | `/api/stock-transactions/{id}` | | StockTransactionResponseDto |

**Request:** `warehouseId*` (number), `productId*` (number), `transactionType*` (`IN`/`OUT`/`RESERVE`/`RELEASE`), `qty*` (>0), `statusId*` (number), `handoverId`, `approvedById`, `refNo` ≤100
**Response:** `id`, `warehouseId`, `productId`, `handoverId`, `transactionType`, `qty`, `qtyBefore`, `qtyAfter`, `reservedQty`, `refNo`, `statusId`, `statusCode`, `approvedById`, `postedAt`

| Type | ធ្វើអ្វី | 409 ពេល |
|---|---|---|
| IN | on-hand + qty | |
| OUT | on-hand − qty | qty > (on-hand − reserved) |
| RESERVE | reserved + qty | qty > (on-hand − reserved) |
| RELEASE | reserved − qty | qty > reserved |

## 16. Settlements `/api/settlements`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/settlements` | `ccy`, `status`, `from`, `to`, pageable | Page |
| POST | `/api/settlements` | `{ settlementDate*, ccy*, totalAmount* }` | 201 SettlementResponseDto |
| GET | `/api/settlements/{id}` | | SettlementResponseDto |
| PUT | `/api/settlements/{id}` | ដូច POST (បានតែ PENDING) | SettlementResponseDto |
| DELETE | `/api/settlements/{id}` | (បានតែ PENDING/FAILED) | 204 |
| PATCH | `/api/settlements/{id}/status` | `{ status* }` | SettlementResponseDto |

`ccy` ៣ អក្សរ (USD/KHR) | `totalAmount` > 0 | `settlementDate + ccy` ស្ទួន → 409
**Response:** `id`, `settlementDate`, `ccy`, `totalAmount`, `status`, `processAt`
**Status flow:** PENDING→PROCESSING, PROCESSING→COMPLETED/FAILED, FAILED→PENDING

## 17. Farmer Payments `/api/farmer-payments`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/farmer-payments` | `accountId`, `settlementId`, `status`, `keyword`, pageable | Page |
| POST | `/api/farmer-payments` | `{ accountId*, settlementId* }` | FarmerPaymentResponseDto |
| GET | `/api/farmer-payments/{id}` | | FarmerPaymentResponseDto |
| PUT | `/api/farmer-payments/{id}` | `{ accountId* }` | FarmerPaymentResponseDto |
| DELETE | `/api/farmer-payments/{id}` | | |
| PATCH | `/api/farmer-payments/{id}/status` | `{ status* }` | FarmerPaymentResponseDto |
| GET | `/api/farmer-payments/ref/{transactionRef}` | | FarmerPaymentResponseDto |

**Response:** `id`, `accountId`, `accountName`, `bankName`, `accountNumber`, `userId`, `userFullName`, `settlementId`, `amount`, `ccy`, `status`, `transactionRef`, `paidAt`

## 18. Customers `/api/customers`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/customers` | `keyword`, `status`, pageable | Page |
| POST | `/api/customers` | CustomerRequestDto | CustomerResponseDto |
| GET | `/api/customers/{id}` | | CustomerResponseDto |
| PUT | `/api/customers/{id}` | CustomerRequestDto | CustomerResponseDto |
| DELETE | `/api/customers/{id}` | | |

**Request:** `fullNameKh*`, `fullNameEn*`, `email*`, `password` (6-100), `status`, `phoneNumber` (`^[0-9+\-\s]{8,15}$`), `image`
**Response:** `id`, `fullNameKh`, `fullNameEn`, `email`, `status`, `phoneNumber`, `image`

## 19. Orders `/api/orders`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/orders` | `customerId`, `status`, `keyword`, pageable | Page |
| POST | `/api/orders` | `{ customerId*, deliveryAddress* ≤255, totalAmount* }` | OrderResponseDto |
| GET | `/api/orders/{id}` | | OrderResponseDto |
| PUT | `/api/orders/{id}` | `{ deliveryAddress*, totalAmount* }` | OrderResponseDto |
| DELETE | `/api/orders/{id}` | | |
| PATCH | `/api/orders/{id}/status` | `{ status* }` | OrderResponseDto |
| GET | `/api/orders/number/{orderNumber}` | | OrderResponseDto |

**Response:** `id`, `customerId`, `orderNumber`, `orderStatus`, `totalAmount`, `deliveryAddress`, `statusId`

## 20. Order Details `/api/order-details`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/order-details` | `orderId`, `page`, `size` | OrderDetailResponseDto[] |
| POST | `/api/order-details` | OrderDetailRequestDto | OrderDetailResponseDto |
| GET | `/api/order-details/{id}` | | OrderDetailResponseDto |
| PUT | `/api/order-details/{id}` | OrderDetailRequestDto | OrderDetailResponseDto |
| DELETE | `/api/order-details/{id}` | | |

**Request:** `orderId*`, `productId*`, `qty*` (≥1), `unitPrice*`, `paymentMethodId*`, `otherMethodId`
**Response:** `id`, `orderId`, `productId`, `qty`, `unitPrice`, `subTotal`, `otherMethodId`, `paymentMethodId`, `createdAt`, `updatedAt`

## 21. Order Status Histories `/api/order-status-histories`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/order-status-histories` | `orderId`, `page`, `size` | OrderStatusHistoryResponseDto[] |
| POST | `/api/order-status-histories` | `{ orderId*, newStatusId*, orderStatusId, remark, changeAt }` | OrderStatusHistoryResponseDto |
| GET | `/api/order-status-histories/{id}` | | OrderStatusHistoryResponseDto |
| PUT | `/api/order-status-histories/{id}` | ដូច POST | OrderStatusHistoryResponseDto |
| DELETE | `/api/order-status-histories/{id}` | | |

**Response:** `id`, `orderId`, `orderStatusId`, `newStatusId`, `remark`, `changeAt`, `createdAt`, `updatedAt`

## 22. Payments `/api/payments`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/payments` | `orderId`, `customerId`, `status`, `keyword`, pageable | Page |
| POST | `/api/payments` | `{ orderId*, paymentMethodId* }` | PaymentResponseDto |
| GET | `/api/payments/{id}` | | PaymentResponseDto |
| PUT | `/api/payments/{id}` | `{ paymentMethodId* }` | PaymentResponseDto |
| DELETE | `/api/payments/{id}` | | |
| PATCH | `/api/payments/{id}/status` | `{ status* }` | PaymentResponseDto |
| GET | `/api/payments/ref/{ref}` | | PaymentResponseDto |

**Response:** `id`, `orderId`, `customerId`, `ref`, `amount`, `paymentMethodId`, `paymentMethod`, `paymentStatusId`, `paymentStatus`, `paidAt`

## 23. Payment Methods `/api/payment-methods`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/payment-methods` | `keyword`, pageable | Page |
| GET | `/api/payment-methods/all` | | PaymentMethodResponseDto[] |
| POST | `/api/payment-methods` | `{ code*, title*, description }` | PaymentMethodResponseDto |
| GET | `/api/payment-methods/{id}` | | PaymentMethodResponseDto |
| PUT | `/api/payment-methods/{id}` | `{ title*, description }` | PaymentMethodResponseDto |
| DELETE | `/api/payment-methods/{id}` | | |

**Response:** `id`, `code`, `title`, `description` | `code`: `[A-Za-z0-9_]` ≤50, `title` ≤100

## 24. Delivery Fees `/api/delivery-fees`

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/api/delivery-fees` | `keyword`, `page`, `size` | FeeDeliveryResponseDto[] |
| POST | `/api/delivery-fees` | `{ code*, amount*, description }` | FeeDeliveryResponseDto |
| GET | `/api/delivery-fees/{id}` | | FeeDeliveryResponseDto |
| PUT | `/api/delivery-fees/{id}` | ដូច POST | FeeDeliveryResponseDto |
| DELETE | `/api/delivery-fees/{id}` | | |

**Response:** `id`, `code`, `amount`, `description`, `createdAt`, `updatedAt`

## 25. Deliveries `/api/deliveries`

| Method | Path | Query | Response |
|---|---|---|---|
| GET | `/api/deliveries` | `orderId`, `statusId`, `page`, `size` | DeliveryResponseDto[] |

**Response:** `id`, `orderId`, `assignTo`, `deliveryAddress`, `deliveryFeeId`, `scheduleAt`, `deliveryAt`, `statusId`, `remarks`, `createdAt`, `updatedAt`

---

`*` = ចាំបាច់ (required)

---

## 26. Uploads `/api/uploads` (JWT)

| Method | Path | Body | Response |
|---|---|---|---|
| POST | `/api/uploads` | multipart form, field `file` | 201 `{ "url": "/uploads/<uuid>.jpg" }` |
| GET | `/uploads/{name}` | (public, no token) | the image |

- Allowed: JPEG, PNG, WEBP (checked by file content). Max 5 MB. Other files → 415. Too big → 413.
- Use the returned `url` in `CategoryRequest.image` or as a product image path (`images: [...]`).
- Relative `/uploads/...` paths: prefix them with the API base URL to load them.

## 27. Product images & visibility (changes)

- `POST /api/products` accepts at most **5** images (400 otherwise).
- `GET /api/products` and `GET /api/products/{id}`: products of farmers whose `approvalStatus` is `PENDING` or `REJECTED` are **hidden** from guests and buyers. The owner sees their own products; ADMIN sees all.

## 28. Farmer approval `/api/users/{id}/approval` (ADMIN only)

| Method | Path | Body | Response |
|---|---|---|---|
| PATCH | `/api/users/{id}/approval` | `{ "status": "APPROVED" \| "PENDING" \| "REJECTED" }` | `{ "id", "approvalStatus" }` |

- Only FARMER accounts (roleId 2) can be approved (400 otherwise). Non-admin → 403.
- New farmers start `PENDING`. Set env `FARMER_AUTO_APPROVE=true` to approve them on registration (dev/test only).
- `POST /api/auth/login` now returns `userId` and `approvalStatus`.
- Self-registration allows only roleId `2` (FARMER) and `3` (BUYER). roleId `1` → 403.

## 29. Commission `/api/settings/commission`

| Method | Path | Body | Response |
|---|---|---|---|
| GET | `/api/settings/commission` | — (logged-in user) | `{ "rate": 0 }` |
| PUT | `/api/settings/commission` | `{ "rate": 0.10 }` (ADMIN only) | `{ "rate": 0.1 }` |

- `rate` is a fraction: `0.10` = 10%. Valid range `0 ≤ rate < 1`. Default `0`.
- When an order becomes `DELIVERED`, every order-detail line is snapshotted:
  `commissionRate`, `commissionAmount = round2(subTotal × rate)`, `farmerNetAmount = subTotal − commissionAmount`.
  Delivery fee is not part of the commission base. Later rate changes do not change delivered lines.
- `OrderDetailResponseDto` now includes `commissionRate`, `commissionAmount`, `farmerNetAmount` (null until delivered).

## 30. Category image

- `CategoryRequest` / `CategoryResponse` have `image` (URL from `/api/uploads`). On update, omitting `image` keeps the current picture.
