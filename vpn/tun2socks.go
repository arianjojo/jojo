package vpn

import (
    "github.com/xjasonlyu/tun2socks"
    "github.com/xjasonlyu/tun2socks/core"
    "log"
)

type ZyrlnTun2Socks struct {
    tunDevice io.ReadWriteCloser
    lwipStack core.Stack
}

func NewTun2Socks(tunFd int, proxyAddr string) (*ZyrlnTun2Socks, error) {
    // ساخت TUN device از فایل دیسکریپتور
    tun, err := tun2socks.OpenTunDevice(tunFd)
    if err != nil {
        return nil, err
    }
    
    // تنظیم SOCKS5 proxy (همون Zyrln listener)
    stack := tun2socks.NewLWIPStack()
    socksHandler := tunnel.NewSocks5Handler(proxyAddr)
    core.RegisterStack(stack, socksHandler)
    
    return &ZyrlnTun2Socks{
        tunDevice: tun,
        lwipStack: stack,
    }, nil
}

func (z *ZyrlnTun2Socks) Start() {
    go tun2socks.Forward(z.tunDevice, z.lwipStack)
}