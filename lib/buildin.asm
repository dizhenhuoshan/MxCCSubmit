





default rel

global __string__concat
global __string__equal
global __string__nequal
global __string__less
global __string__lessEqual
global __print
global __println
global __printForInt
global __printlnForInt
global __getString
global __getInt
global __toString
global __class__string__substring
global __class__string__parseInt
global __class__string__ord

extern strlen
extern strcmp
extern stdout
extern stdin
extern puts
extern printf
extern memcpy
extern malloc
extern __isoc99_scanf
extern _IO_putc
extern _IO_getc


SECTION .text

__string__concat:
        push    rbp
        push    r15
        push    r14
        push    r13
        push    r12
        push    rbx
        push    rax
        mov     r14, rsi
        mov     r13, rdi
        mov     rbp, qword [r13]
        mov     r12, qword [r14]
        lea     rbx, [r12+rbp]
        mov     rax, rbx
        shl     rax, 32
        mov     rdi, qword 900000000H
        add     rdi, rax
        sar     rdi, 32
        call    malloc
        mov     r15, rax
        movsxd  rax, ebx
        mov     qword [r15], rax
        test    ebp, ebp
        jle     L_003
        lea     rdi, [r15+8H]
        add     r13, 8
        lea     ebx, [rbp-1H]
        mov     edx, ebp
        mov     rsi, r13
        call    memcpy
        test    r12d, r12d
        jle     L_002
L_001:  add     r14, 8
        movsxd  rbx, ebx
        lea     rdi, [r15+rbx]
        add     rdi, 9
        mov     edx, r12d
        mov     rsi, r14
        call    memcpy
        add     ebx, r12d
L_002:  movsxd  rax, ebx
        mov     byte [r15+rax+9H], 0
        mov     rax, r15
        add     rsp, 8
        pop     rbx
        pop     r12
        pop     r13
        pop     r14
        pop     r15
        pop     rbp
        ret


L_003:
        mov     ebx, 4294967295
        test    r12d, r12d
        jg      L_001
        jmp     L_002






ALIGN   16

__string__equal:
        push    rax
        add     rdi, 8
        add     rsi, 8
        call    strcmp
        xor     ecx, ecx
        test    eax, eax
        sete    cl
        mov     eax, ecx
        pop     rcx
        ret






ALIGN   8

__string__nequal:
        push    rax
        add     rdi, 8
        add     rsi, 8
        call    strcmp
        xor     ecx, ecx
        test    eax, eax
        setne   cl
        mov     eax, ecx
        pop     rcx
        ret






ALIGN   8

__string__less:
        push    rax
        add     rdi, 8
        add     rsi, 8
        call    strcmp
        shr     eax, 31
        pop     rcx
        ret







ALIGN   16

__string__lessEqual:
        push    rax
        add     rdi, 8
        add     rsi, 8
        call    strcmp
        xor     ecx, ecx
        test    eax, eax
        setle   cl
        mov     eax, ecx
        pop     rcx
        ret






ALIGN   8

__print:
        lea     rsi, [rdi+8H]
        mov     edi, L_037
        xor     eax, eax
        jmp     printf


__println:
        add     rdi, 8
        jmp     puts






ALIGN   8

__printForInt:
        push    rbx
        sub     rsp, 48
        mov     ebx, edi
        test    ebx, ebx
        jz      L_008
        jns     L_004
        neg     ebx
        mov     rsi, qword [rel stdout]
        mov     edi, 45
        call    _IO_putc
L_004:  xor     eax, eax
        mov     ecx, 3435973837





ALIGN   16
L_005:  mov     edx, ebx
        imul    rdx, rcx
        shr     rdx, 35
        lea     esi, [rdx+rdx]
        lea     esi, [rsi+rsi*4]
        mov     edi, ebx
        sub     edi, esi
        mov     dword [rsp+rax*4], edi
        add     rax, 1
        cmp     ebx, 9
        mov     ebx, edx
        ja      L_005
        test    eax, eax
        jle     L_007
        movsxd  rbx, eax
        add     rbx, 1




ALIGN   8
L_006:  mov     edi, dword [rsp+rbx*4-8H]
        add     edi, 48
        mov     rsi, qword [rel stdout]
        call    _IO_putc
        add     rbx, -1
        cmp     rbx, 1
        jg      L_006
L_007:  add     rsp, 48
        pop     rbx
        ret


L_008:
        mov     rsi, qword [rel stdout]
        mov     edi, 48
        call    _IO_putc
        add     rsp, 48
        pop     rbx
        ret





ALIGN   8

__printlnForInt:
        push    rbx
        sub     rsp, 48
        mov     ebx, edi
        test    ebx, ebx
        jz      L_012
        jns     L_009
        neg     ebx
        mov     rsi, qword [rel stdout]
        mov     edi, 45
        call    _IO_putc
L_009:  xor     eax, eax
        mov     ecx, 3435973837





ALIGN   16
L_010:  mov     edx, ebx
        imul    rdx, rcx
        shr     rdx, 35
        lea     esi, [rdx+rdx]
        lea     esi, [rsi+rsi*4]
        mov     edi, ebx
        sub     edi, esi
        mov     dword [rsp+rax*4], edi
        add     rax, 1
        cmp     ebx, 9
        mov     ebx, edx
        ja      L_010
        test    eax, eax
        jle     L_013
        movsxd  rbx, eax
        add     rbx, 1




ALIGN   8
L_011:  mov     edi, dword [rsp+rbx*4-8H]
        add     edi, 48
        mov     rsi, qword [rel stdout]
        call    _IO_putc
        add     rbx, -1
        cmp     rbx, 1
        jg      L_011
        jmp     L_013

L_012:  mov     rsi, qword [rel stdout]
        mov     edi, 48
        call    _IO_putc
L_013:  mov     rsi, qword [rel stdout]
        mov     edi, 10
        call    _IO_putc
        add     rsp, 48
        pop     rbx
        ret







ALIGN   16

__getString:
        push    r14
        push    rbx
        push    rax
        mov     edi, 266
        call    malloc
        mov     rbx, rax
        lea     r14, [rbx+8H]
        mov     edi, L_037
        xor     eax, eax
        mov     rsi, r14
        call    __isoc99_scanf
        mov     rdi, r14
        call    strlen
        mov     qword [rbx], rax
        mov     rax, rbx
        add     rsp, 8
        pop     rbx
        pop     r14
        ret






ALIGN   8

__getInt:
        push    rbp
        push    r15
        push    r14
        push    rbx
        push    rax
        mov     rdi, qword [rel stdin]
        call    _IO_getc
        mov     ecx, eax
        shl     ecx, 24
        lea     edx, [rcx-30000000H]
        cmp     edx, 150994945
        jc      L_016
        xor     edx, edx




ALIGN   16
L_014:  mov     r15b, 1
        cmp     ecx, 754974720
        jz      L_015
        mov     r15d, edx
L_015:  mov     rdi, qword [rel stdin]
        call    _IO_getc
        mov     ecx, eax
        shl     ecx, 24
        lea     esi, [rcx-30000000H]
        mov     edx, r15d
        cmp     esi, 150994944
        ja      L_014
        and     r15b, 01H
        jmp     L_017

L_016:  xor     r15d, r15d
L_017:  movzx   r14d, al
        mov     rdi, qword [rel stdin]
        call    _IO_getc
        lea     ecx, [r14-30H]
        mov     edx, eax
        shl     edx, 24
        add     edx, -788529153
        cmp     edx, 184549374
        ja      L_019
L_018:  movzx   ebx, al
        lea     ebp, [rcx+rcx*4]
        mov     rdi, qword [rel stdin]
        call    _IO_getc
        lea     ecx, [rbx+rbp*2]
        add     ecx, -48
        mov     edx, eax
        shl     edx, 24
        add     edx, -788529153
        cmp     edx, 184549375
        jc      L_018
        lea     r14d, [rbx+rbp*2]
L_019:  mov     eax, 48
        sub     eax, r14d
        test    r15b, r15b
        cmove   eax, ecx
        add     rsp, 8
        pop     rbx
        pop     r14
        pop     r15
        pop     rbp
        ret






ALIGN   16

__toString:
        push    rbp
        push    r15
        push    r14
        push    r12
        push    rbx
        sub     rsp, 48
        mov     r14d, edi
        mov     eax, r14d
        neg     eax
        cmovl   eax, r14d
        shr     r14d, 31
        test    eax, eax
        jz      L_021
        xor     ebx, ebx





ALIGN   16
L_020:  movsxd  rcx, eax
        imul    rax, rcx, 1717986919
        mov     rdx, rax
        shr     rdx, 63
        sar     rax, 34
        add     eax, edx
        lea     edx, [rax+rax]
        lea     edx, [rdx+rdx*4]
        mov     esi, ecx
        sub     esi, edx
        mov     dword [rsp+rbx*4+4H], esi
        add     rbx, 1
        add     ecx, 9
        cmp     ecx, 18
        ja      L_020
        jmp     L_022

L_021:  mov     dword [rsp+4H], 0
        mov     ebx, 1
L_022:  mov     ebp, ebx
        mov     r15d, r14d
        lea     r12, [r15+rbp]
        lea     edi, [r12+9H]
        call    malloc
        mov     qword [rax], r12
        mov     byte [rax+r12+8H], 0
        test    r14d, r14d
        jz      L_023
        mov     byte [rax+8H], 45
L_023:  cmp     ebx, 1
        mov     r8d, 1
        cmova   r8, rbp
        cmp     r8, 7
        jbe     L_024
        cmp     rbp, 1
        mov     ecx, 1
        cmova   rcx, rbp
        add     rcx, -1
        cmp     rcx, 2147483647
        jbe     L_028
L_024:  xor     ecx, ecx
L_025:  sub     ebx, ecx
        lea     rsi, [rax+r15]
        add     rsi, 8




ALIGN   8
L_026:  movsxd  rbx, ebx
        movzx   edx, byte [rsp+rbx*4]
        add     dl, 48
        mov     byte [rsi+rcx], dl
        add     rcx, 1
        add     ebx, -1
        cmp     rcx, rbp
        jc      L_026
L_027:  add     rsp, 48
        pop     rbx
        pop     r12
        pop     r14
        pop     r15
        pop     rbp
        ret


L_028:
        mov     ecx, r8d
        and     ecx, 0FFFFFFF8H
        xor     edx, edx
        movdqa  xmm0, oword [rel .LCPI11_0]
        movdqa  xmm1, oword [rel .LCPI11_1]
        mov     edi, ebx
L_029:  movsxd  rdi, edi
        movdqu  xmm2, oword [rsp+rdi*4-1CH]
        movdqu  xmm3, oword [rsp+rdi*4-0CH]
        pshufd  xmm3, xmm3, 1BH
        pshufd  xmm2, xmm2, 1BH
        paddd   xmm3, xmm0
        paddd   xmm2, xmm0
        mov     rsi, rdx
        or      rsi, r15
        pand    xmm3, xmm1
        packuswb xmm3, xmm3
        packuswb xmm3, xmm3
        movd    dword [rax+rsi+8H], xmm3
        pand    xmm2, xmm1
        packuswb xmm2, xmm2
        packuswb xmm2, xmm2
        movd    dword [rax+rsi+0CH], xmm2
        add     rdx, 8
        add     edi, -8
        cmp     rcx, rdx
        jnz     L_029
        cmp     r8, rcx
        jne     L_025
        jmp     L_027






ALIGN   16

__class__string__substring:
        push    rbp
        push    r15
        push    r14
        push    r13
        push    r12
        push    rbx
        push    rax
        mov     r13d, edx
        mov     r14d, esi
        mov     r15, rdi
        mov     eax, r13d
        sub     eax, r14d
        movsxd  rbx, eax
        lea     r12, [rbx+1H]
        lea     eax, [rbx+0AH]
        movsxd  rdi, eax
        call    malloc
        mov     rbp, rax
        mov     qword [rbp], r12
        test    ebx, ebx
        js      L_030
        mov     rdi, rbp
        add     rdi, 8
        movsxd  rax, r14d
        lea     rsi, [r15+rax]
        add     rsi, 8
        add     r13d, 1
        sub     r13d, r14d
        mov     rdx, r13
        call    memcpy
L_030:  mov     byte [rbp+r12+8H], 0
        mov     rax, rbp
        add     rsp, 8
        pop     rbx
        pop     r12
        pop     r13
        pop     r14
        pop     r15
        pop     rbp
        ret


__class__string__parseInt:
        mov     sil, byte [rdi+8H]
        mov     eax, esi
        add     al, -48
        cmp     al, 10
        jc      L_033
        xor     ecx, ecx
        mov     eax, 9





ALIGN   16
L_031:  mov     r9b, 1
        cmp     sil, 45
        jz      L_032
        mov     r9d, ecx
L_032:  movzx   esi, byte [rdi+rax]
        mov     edx, esi
        add     dl, -48
        add     rax, 1
        mov     ecx, r9d
        cmp     dl, 9
        ja      L_031
        add     eax, -8
        and     r9b, 01H
        jmp     L_034

L_033:  mov     eax, 1
        xor     r9d, r9d
L_034:  movsx   r8d, sil
        lea     r10d, [r8-30H]
        mov     dl, byte [rdi+rax+8H]
        mov     ecx, edx
        add     cl, -48
        cmp     cl, 9
        ja      L_036
        add     rax, rdi
        add     rax, 9
        nop
L_035:  movsx   edi, dl
        lea     esi, [r10+r10*4]
        lea     r10d, [rdi+rsi*2]
        add     r10d, -48
        movzx   edx, byte [rax]
        mov     ecx, edx
        add     cl, -48
        add     rax, 1
        cmp     cl, 10
        jc      L_035
        lea     r8d, [rdi+rsi*2]
L_036:  mov     eax, 48
        sub     eax, r8d
        test    r9b, r9b
        cmove   eax, r10d
        ret







ALIGN   16

__class__string__ord:
        movsxd  rax, esi
        movsx   eax, byte [rdi+rax+8H]
        ret


SECTION .rodata.cst16

ALIGN   16
L_028.LCPI11_0:
        dq 0000003000000030H
        dq 0000003000000030H

L_028.LCPI11_1:
        dq 000000FF000000FFH
        dq 000000FF000000FFH


SECTION .rodata.str1.1 

L_037:
        db 25H, 73H, 00H
