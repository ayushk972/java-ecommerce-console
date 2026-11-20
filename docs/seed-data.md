# Product Seed Data

## Overview

This document describes the initial product data used to populate the e-commerce product catalogue.

The seed data is maintained in `ProductSeedData.java` and contains 15 predefined products.

## Product Catalogue

| Product ID | Product Name | Category | Price (₹) | Stock | Status |
|---|---|---|---:|---:|---|
| PRD-1001 | Laptop Pro 15 | Electronics | 55,000.00 | 10 | ACTIVE |
| PRD-1002 | Smartphone X | Electronics | 22,000.00 | 5 | ACTIVE |
| PRD-1003 | Wireless Headphones | Electronics | 2,500.00 | 0 | ACTIVE |
| PRD-1004 | Bluetooth Speaker | Electronics | 1,800.00 | 3 | ACTIVE |
| PRD-1005 | Mechanical Keyboard | Electronics | 3,200.00 | 8 | ACTIVE |
| PRD-1006 | Java Programming Book | Books | 650.00 | 20 | ACTIVE |
| PRD-1007 | Data Structures Book | Books | 750.00 | 0 | ACTIVE |
| PRD-1008 | Database Management Book | Books | 550.00 | 12 | ACTIVE |
| PRD-1009 | Men Cotton T-Shirt | Fashion | 499.00 | 15 | ACTIVE |
| PRD-1010 | Running Shoes | Fashion | 1,999.00 | 4 | ACTIVE |
| PRD-1011 | Casual Backpack | Fashion | 899.00 | 7 | ACTIVE |
| PRD-1012 | Wrist Watch | Fashion | 1,499.00 | 10 | ACTIVE |
| PRD-1013 | Electric Kettle | Home and Kitchen | 999.00 | 6 | ACTIVE |
| PRD-1014 | Mixer Grinder | Home and Kitchen | 2,499.00 | 9 | ACTIVE |
| PRD-1015 | Old Model Speaker | Home and Kitchen | 1,200.00 | 5 | HIDDEN |

## Seed Data Summary

| Metric | Value |
|---|---:|
| Total Products | 15 |
| Total Categories | 4 |
| Electronics | 5 |
| Books | 3 |
| Fashion | 4 |
| Home and Kitchen | 3 |
| Out-of-stock Products | 2 |
| Low-stock Products (1–5 units) | 5 |
| Hidden Products | 1 |
| Active Products | 14 |

## Product ID Generation

Product IDs are generated using the injected `IdGenerator`.

- Prefix: `PRD-`
- Starting number: `1001`
- Final number: `1015`

## Seeding Behaviour

- Products are inserted into the product repository.
- Existing products with matching normalized names are skipped.
- Calling `seedProducts()` again does not insert duplicate products.
- Product IDs are generated only for products that are actually inserted.
- All products are initially ACTIVE, except the predefined hidden product.
- Seed operations do not generate audit records.
