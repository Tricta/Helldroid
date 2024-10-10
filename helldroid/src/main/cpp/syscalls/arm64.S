.section .text
   .globl Helldroid_openat
   .globl Helldroid_close

   .globl Helldroid_memset
   .globl Helldroid_read

   .globl Helldroid_socket
   .globl Helldroid_htons
   .globl Helldroid_htonl
   .globl Helldroid_connect
   .globl Helldroid_send

   .globl Helldroid_strcmp
   .globl Helldroid_strncmp
   .globl Helldroid_getdents64

   .globl Helldroid_inotify_init1
   .globl Helldroid_inotify_add_watch
   .globl Helldroid_inotify_rm_watch

   .globl Helldroid_getpid
   .globl Helldroid_kill

   .globl Helldroid_lseek

Helldroid_openat:
    mov x4, x0
    mov x0, x3
    mov x3, x1
    mov x1, x2
    mov x2, x4

    mov x8, #56
    svc #0
    ret

Helldroid_close:
    ldr x4, [x0]
    ldr x2, =0x6C6C6548
    cmp x4, x2
    b.ne Error_Close
    mov x0, x1
    mov x8, #57
    mov x4, xzr
    svc 0
    ret
Error_Close:
    mov x0, -1
    ret

Helldroid_memset:
    eor x10, xzr, xzr
set:
    cmp x2, x10
	ble decrement_rdi
    strb w1, [x0], #1
    add x10, x10, #1
    b set
decrement_rdi:
	cmp x10, #0
	beq end
	sub x0, x0, x2
	sub x10, x10, x2
	b decrement_rdi
end:
	mov x8, x0
	ret

Helldroid_read:
    mov x8, #63
    svc 0
    ret

Helldroid_socket:
    mov x8, #198
    svc 0
    ret

Helldroid_htons:
    rev16 w0, w0
    ret

Helldroid_htonl:
    rev x0, x0
    ret

Helldroid_connect:
    mov x8, #203
    svc 0
    ret

Helldroid_send:
    mov x8, #64
    svc 0
    ret

Helldroid_strcmp:
    ldrb w3, [x0]
    ldrb w4, [x1]
    cbz w3, endStrcmp
    cbz w4, endStrcmp
    cmp w3, w4
    b.ne endStrcmp
    add x0, x0, #1
    add x1, x1, #1
    b Helldroid_strcmp
endStrcmp:
    mov w0, w3
    sub w0, w0, w4
    ret

Helldroid_strncmp:
    stp x29, x30, [sp, -16]!
    mov x29, sp
    mov x3, xzr
    mov w10, wzr
    mov w11, wzr
strncmp_check:
    cmp x3, x2
    b.ge strncmp_end
    ldrb w10, [x0, x3]
    ldrb w11, [x1, x3]
    cmp w10, w11
    b.ne strncmp_end
    cbz w10, strncmp_end
    add x3, x3, #1
    b strncmp_check
strncmp_end:
    sub w10, w10, w11
    sxtb x0, w10
    mov sp, x29
    ldp x29, x30, [sp], 16
    ret

Helldroid_getdents64:
    mov x8, #61
    svc 0
    ret

Helldroid_getpid:
    mov x8, #172
    svc 0
    ret

Helldroid_kill:
    mov x8, #129
    svc 0
    ret

Helldroid_inotify_init1:
    mov x8, #26
    svc 0
    ret

Helldroid_inotify_add_watch:
    mov x8, #27
    svc 0
    ret

Helldroid_inotify_rm_watch:
    mov x8, #28
    svc 0
    ret

Helldroid_lseek:
    mov x8, #62
    svc 0
    ret
