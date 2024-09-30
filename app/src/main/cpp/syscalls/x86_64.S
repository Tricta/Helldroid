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
    push r10
    push rdx
    push rdi
    push rsi

    pop r10
    pop rdx
    pop rsi
    pop rdi

    mov rax, 257
    syscall
    ret

Helldroid_close:
    mov ecx, dword ptr [rdi]
    cmp ecx, 0x6C6C6548
    jne Error_Close
    mov rdi, rsi
    mov rax, 3
    xor rcx, rcx
    syscall
    ret
Error_Close:
    mov rax, -1
    ret

Helldroid_memset:
    xor rcx, rcx
set:
    cmp rdx, rcx
	jle decrement_rdi
    mov byte ptr [rdi], sil
    inc rdi
    inc rcx
    jmp set
decrement_rdi:
	cmp rcx, 0
	je end
	sub rdi, rdx
	sub rcx, rdx
	jmp decrement_rdi
end:
	mov rax, rdi
	ret

Helldroid_read:
    mov rax, 0
    syscall
    ret

Helldroid_socket:
    mov rax, 41
    syscall
    ret

Helldroid_htons:
    mov dx, di
    mov ax, dx
    shl ax, 8
    shr dx, 8
    mov al, dl
    ret

Helldroid_htonl:
    mov edx, edi
    mov eax, edx
    bswap eax
    ret

Helldroid_connect:
    mov rax, 42
    syscall
    ret

Helldroid_send:
    mov rax, 44
    syscall
    ret

Helldroid_strcmp:
    xor rbx, rbx
    xor rdx, rdx
    mov bl, byte ptr [rdi]
    mov dl, byte ptr [rsi]
    cmp bl, 0
    je endStrcmp
    cmp dl, 0
    je endStrcmp
    cmp bl, dl
    jne endStrcmp
    inc rdi
    inc rsi
    jmp Helldroid_strcmp
endStrcmp:
    xor rax, rax
    mov al, bl
    xor rcx, rcx
    mov cl, dl
    sub al, cl
    ret

Helldroid_strncmp:
    push rbp
    mov rbp, rsp
    xor rcx, rcx
   	xor r10b, r10b
    xor r11b, r11b
strncmp_check:
	cmp rcx, rdx
	jge strncmp_end
	mov r10b, [rdi + rcx]
	mov r11b, [rsi + rcx]
	cmp r10b, r11b
	jne strncmp_end
	cmp r10b, 0
	je strncmp_end
	inc rcx
	jmp strncmp_check
strncmp_end:
	sub r10b, r11b
	movsx rax, r10b
	mov rsp, rbp
	pop rbp
	ret

Helldroid_getdents64:
    mov rax, 217
    syscall
    ret

Helldroid_getpid:
    mov rax, 39
    syscall
    ret

Helldroid_kill:
    mov rax, 62
    syscall
    ret

Helldroid_inotify_init1:
    mov rax, 294
    syscall
    ret

Helldroid_inotify_add_watch:
    mov rax, 254
    syscall
    ret

Helldroid_inotify_rm_watch:
    mov rax, 255
    syscall
    ret

Helldroid_lseek:
    mov rax, 8
    syscall
    ret